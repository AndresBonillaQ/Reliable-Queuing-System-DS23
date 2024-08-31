package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.model.response.utils.DesStatusEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.request.CommitLogRequest;
import it.polimi.ds.message.raft.request.HeartbeatRequest;
import it.polimi.ds.message.raft.request.RaftLogEntryRequest;
import it.polimi.ds.network.handler.BrokerRequestDispatcher;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;
import it.polimi.ds.utils.config.Timing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FollowerBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(FollowerBrokerState.class.getName());

    private final AtomicBoolean heartBeatReceived = new AtomicBoolean(false);

    private final Set<Integer> hasAlreadyVoteInTerm = new ConcurrentSkipListSet<>();

    private final Long random = Timing.HEARTBEAT_PERIOD_CHECKING_FROM + (long) (Math.random() * Timing.HEARTBEAT_PERIOD_CHECKING_FROM_TO_SUM);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FollowerBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        heartbeatTimerThreadStart();
    }

    /**
     * This method is not used as Follower but as Leader
     * Waiting to become a Leader
     * in.readLine used to know when server goes down
     * */
    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {
        in.readLine();
    }

    @Override
    public void clientToGatewayExec(BufferedReader in, PrintWriter out) {
    }

    @Override
    public void serverToGatewayExec(BufferedReader in, PrintWriter out) throws IOException {
    }

    /**
     * Followers accept request from leader and requestVote from candidate
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();

        //log.log(Level.INFO, "Request from broker {0}: {1}", new Object[]{clientBrokerId, requestLine});

        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);
        ResponseMessage responseMessage;

        switch (requestMessage.getId()) {
            case APPEND_ENTRY_LOG_REQUEST -> {

                log.log(Level.INFO, "Request from broker {0}: {1}", new Object[]{clientBrokerId, requestLine});

                RaftLogEntryRequest raftLogEntryRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), RaftLogEntryRequest.class);

                if(brokerContext.getBrokerRaftIntegration().processRaftLogEntryRequest(raftLogEntryRequest)){
                    responseMessage = NetworkMessageBuilder.Response.buildAppendEntryLogResponse(
                            StatusEnum.OK,
                            DesStatusEnum.RAFT_LOG_ENTRY_OK.getValue(),
                            -1
                    );
                } else {
                    responseMessage = NetworkMessageBuilder.Response.buildAppendEntryLogResponse(
                            StatusEnum.KO,
                            DesStatusEnum.RAFT_LOG_ENTRY_KO.getValue(),
                            raftLogEntryRequest.getPrevLogIndex()
                    );
                }

                System.out.println("After APPEND_ENTRY_LOG_REQUEST");
                brokerContext.getBrokerRaftIntegration().printLogs();

                //forwarding message
                out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                out.flush();
            }

            // no response
            case COMMIT_REQUEST -> {
                log.log(Level.INFO, "Request from broker {0}: {1}", new Object[]{clientBrokerId, requestLine});

                CommitLogRequest commitLogRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), CommitLogRequest.class);

                brokerContext.getBrokerRaftIntegration().processCommitRequestAndGetRequestsToExec(commitLogRequest.getLastCommitIndex())
                        .forEach(request -> {
                            try {
                                BrokerRequestDispatcher.exec(brokerContext, request);
                            } catch (RequestNoManagedException e) {
                                throw new RuntimeException(e);
                            }
                        });

                System.out.println("After COMMIT_REQUEST");
                brokerContext.getBrokerRaftIntegration().printLogs();

            }

            case HEARTBEAT_REQUEST -> {
                HeartbeatRequest heartbeatRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), HeartbeatRequest.class);
                //log.log(Level.INFO, "HeartBeat of leader {0} received!", heartbeatRequest.getLeaderId());

                if(heartbeatRequest.getTerm() >= brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
                    brokerContext.updateNewLeaderInfo(heartbeatRequest.getLeaderId());
                    brokerContext.getBrokerRaftIntegration().increaseCurrentTerm(heartbeatRequest.getTerm());
                    heartBeatReceived.set(true);

                    responseMessage = NetworkMessageBuilder.Response.buildHeartBeatResponse(StatusEnum.OK, "");
                } else
                    responseMessage = NetworkMessageBuilder.Response.buildHeartBeatResponse(StatusEnum.KO, "My currentTerm is greater, you are not the leader!");


                out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                out.flush();
            }

            case VOTE_REQUEST -> {

                VoteRequest voteRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), VoteRequest.class);
                log.log(Level.INFO, "Received from candidate a term: {0}, myCurrentTerm {1}, hasAlreadyVoteInTerm {2}", new Object[]{voteRequest.getTerm(), brokerContext.getBrokerRaftIntegration().getCurrentTerm(), hasAlreadyVoteInTerm.contains(voteRequest.getTerm())});

                if(voteRequest.getTerm() > brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
                    if(!hasAlreadyVoteInTerm.contains(voteRequest.getTerm())){
                        hasAlreadyVoteInTerm.add(voteRequest.getTerm());
                        responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.OK, "");
                    } else
                        responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "Already voted for this term");
                } else
                    responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "CurrentTerm of follower is greater");


                out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                out.flush();
            }

            default -> {
                //not managed
                System.out.println("DEFAULT");
            }
        }
    }

    @Override
    public void onHeartbeatTimeout(){
        synchronized (brokerContext.getBrokerState()){
            log.log(Level.INFO, "I'm going to elect myself");
            brokerContext.setBrokerState(new CandidateBrokerState(brokerContext));
        }
    }

    private void heartbeatTimerThreadStart(){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                () -> {

                    if (scheduler.isShutdown() || !brokerContext.isBrokerSetUp()) {
                        return;
                    }

                    log.log(Level.INFO, "Is leader alive {0}", heartBeatReceived);
                    if(!heartBeatReceived.get()) {
                        onHeartbeatTimeout();
                        scheduler.shutdownNow();
                    }
                    else
                        heartBeatReceived.set(false);
                },
                Timing.HEARTBEAT_DELAY_CHECKING,
                random,
                TimeUnit.MILLISECONDS
        );
    }
}
