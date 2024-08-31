package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.raft.utils.RaftLog;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.network.gateway.client.ClientToGateway;
import it.polimi.ds.network.gateway.server.ServerToGateway;
import it.polimi.ds.network.handler.BrokerRequestDispatcher;
import it.polimi.ds.network.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;
import it.polimi.ds.utils.config.Timing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(LeaderBrokerState.class.getName());
    private final AtomicBoolean hasAlreadyNotifyGateway = new AtomicBoolean(false);
    private Set<String> brokerIdSetVotedOk = new ConcurrentSkipListSet<>();

    public LeaderBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        log.log(Level.INFO, "I'm becoming leader!!");
        startHeartBeat();
        ExecutorInstance.getInstance().getExecutorService().submit(new ServerToGateway(brokerContext, brokerContext.getMyBrokerConfig().getBrokerServerPortToGateway()));
        ExecutorInstance.getInstance().getExecutorService().submit(new ClientToGateway(brokerContext, brokerContext.getMyBrokerConfig().getGatewayInfo()));
    }

    /**
     * This method handles all messages to forward to followers from the BlockingQueue,
     * messages could be APPEND_LOG_ENTRY_REQUEST or COMMIT_REQUEST
     * */
    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        try{

            if(ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(clientBrokerId) != null){
                //blocking execution until a message to forward is available
                String requestToForward = ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(clientBrokerId).poll(10, TimeUnit.MILLISECONDS);

                if(requestToForward != null && !requestToForward.isEmpty()){
                    //log.log(Level.INFO, "Forwarding request to follower: {0}", requestToForward);

                    //forwarding message
                    out.println(requestToForward);
                    out.flush();

                    RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestToForward, RequestMessage.class);

                    switch (requestMessage.getId()){

                        case HEARTBEAT_REQUEST -> {
                            String responseLine = in.readLine();
                            //log.log(Level.INFO,"HeartBeat response: {0}", responseLine);
                        }

                        case APPEND_ENTRY_LOG_REQUEST -> {
                            String responseLine = in.readLine();
                            ThreadsCommunication.getInstance().addResponseToFollowerResponseQueue(clientBrokerId, responseLine);
                        }

                        case COMMIT_REQUEST -> {

                        }

                        default -> {}
                    }

                }
            }

        }catch (InterruptedException e){
            log.log(Level.SEVERE, "ERROR: {0}", e.getMessage());
        }

    }

    @Override
    public void clientToGatewayExec(BufferedReader in, PrintWriter out) throws IOException {
        if(Boolean.FALSE.equals(hasAlreadyNotifyGateway.get())){

            RequestMessage requestMessage = NetworkMessageBuilder.Request.buildNewLeaderToGatewayRequest(
                    brokerContext.getMyBrokerConfig().getMyClusterId(),
                    brokerContext.getMyBrokerConfig().getMyBrokerId(),
                    brokerContext.getMyBrokerConfig().getMyHostName(),
                    brokerContext.getMyBrokerConfig().getBrokerServerPortToGateway()
            );

            log.log(Level.INFO, "Notifying gateway about new leader... {0}", requestMessage);

            out.println(GsonInstance.getInstance().getGson().toJson(requestMessage));
            out.flush();

            String responseLine = in.readLine();
            log.log(Level.INFO, "Response of notify gateway about new leader... {0}", responseLine);

            hasAlreadyNotifyGateway.set(true);
        }
    }

    @Override
    public void serverToGatewayExec(BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();

        forwardAppendLogRequestToAllFollowers(requestLine);
        brokerIdSetVotedOk = brokerContext.getBrokerRaftIntegration().calculateConsensus();
        handleConsensusOutcome(requestLine, out);

        brokerContext.getBrokerRaftIntegration().printLogs();
        brokerContext.getBrokerModel().printState();
    }

    /**
     * This method handles all responses received from followers
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {
        String requestLine = in.readLine();

        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);

        ResponseMessage responseMessage;
        switch (requestMessage.getId()){

            case VOTE_REQUEST -> {
                VoteRequest voteRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), VoteRequest.class);

                if(voteRequest.getTerm() <= brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
                    log.log(Level.INFO, "Someone tries to become a leader with VOTE_REQUEST but I'm the leader!");
                    responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "I'm the leader!");
                } else {
                    responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.OK, "");
                    log.log(Level.INFO, "I am the leader but has received a VOTE_REQUEST with higher Term! my is {}, received {}", new Object[]{brokerContext.getBrokerRaftIntegration().getCurrentTerm(), voteRequest.getTerm()});
                    log.log(Level.INFO, "Becoming follower...");
                    brokerContext.setBrokerState(new FollowerBrokerState(brokerContext));
                }

                out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                out.flush();
            }

        }
    }

    /**
     * This method handle logic to send HeartBeat to followers
     * */
    private void startHeartBeat(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {

                    if(!brokerContext.isBrokerSetUp())
                        return;

                    RequestMessage requestMessage = NetworkMessageBuilder.Request.buildHeartBeatRequest(brokerContext.getMyBrokerConfig().getMyBrokerId(), brokerContext.getBrokerRaftIntegration().getCurrentTerm());

                    ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(
                            GsonInstance.getInstance().getGson().toJson(requestMessage)
                    );

                }, Timing.HEARTBEAT_DELAY_SENDING, Timing.HEARTBEAT_PERIOD_SENDING, TimeUnit.MILLISECONDS
        );
    }

    private void forwardAppendLogRequestToAllFollowers(String requestLine){

        brokerContext.getBrokerRaftIntegration().buildAndAppendNewLog(requestLine);

        List<RaftLog> raftLogList = brokerContext.getBrokerRaftIntegration().getLastUncommittedLogsToForward();

        final RequestMessage raftLogMessage = NetworkMessageBuilder.Request.buildAppendEntryLogRequest(
                brokerContext.getBrokerRaftIntegration().getCurrentTerm(),
                brokerContext.getMyBrokerConfig().getMyBrokerId(),
                brokerContext.getBrokerRaftIntegration().getPrevCommittedLogIndex(),
                brokerContext.getBrokerRaftIntegration().getPrevLogTerm(brokerContext.getBrokerRaftIntegration().getPrevCommittedLogIndex()),
                raftLogList
        );

        log.log(Level.INFO, "Forwarding to all followers {0}", raftLogMessage);

        // passing message to each thread which handle client connection with followers
        ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(GsonInstance.getInstance().getGson().toJson(raftLogMessage));
    }

    private void handleConsensusOutcome(String requestLine, PrintWriter out){
        if(isConsensusReached())
            handleConsensusReached(requestLine, out);
        else
            handleConsensusNoReached(requestLine, out);
    }

    private boolean isConsensusReached(){
        final int numFollowersAlive = ThreadsCommunication.getInstance().getNumThreadsOfAliveBrokers();
        final int numVotedOk = brokerIdSetVotedOk.size() + 1;   //+1 because voted for myself

        return (numFollowersAlive > 0 && numVotedOk >= Math.floorDiv(brokerContext.getNumClusterBrokers(), 2) + 1) ||
                (numFollowersAlive == 0 && brokerContext.getNumClusterBrokers() == 1);
    }

    private void handleConsensusReached(String requestLine, PrintWriter out){
        log.log(Level.INFO, "Consensus reached, executing command and responding to gateway..");

        try {
            // exec the request locally
            ResponseMessage response = BrokerRequestDispatcher.exec(brokerContext, requestLine);

            // send commit to gateway
            out.println(GsonInstance.getInstance().getGson().toJson(response));
            out.flush();

        } catch (RequestNoManagedException e) {
            log.log(Level.SEVERE, "Request {} not managed!", requestLine);
        }

        // increase my lastCommitIndex and commit log
        brokerContext.getBrokerRaftIntegration().handleLastLogsAppended();

        // send commit msg to all followers that voted ok!
        RequestMessage requestMessage = NetworkMessageBuilder.Request.buildCommitRequest(brokerContext.getBrokerRaftIntegration().getLastCommitIndex());
        brokerIdSetVotedOk.forEach(x -> ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(x).add(GsonInstance.getInstance().getGson().toJson(requestMessage)));
    }

    private void handleConsensusNoReached(String requestLine, PrintWriter out){
        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);
        ResponseMessage responseMessage = NetworkMessageBuilder.Response.buildServiceUnavailableResponse(StatusEnum.KO, Const.ResponseDes.KO.UNAVAILABLE_SERVICE_KO, requestMessage.getClientId());
        log.log(Level.INFO, "Consensus Not reached, responding to gateway {0}", responseMessage);
        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();
    }
}
