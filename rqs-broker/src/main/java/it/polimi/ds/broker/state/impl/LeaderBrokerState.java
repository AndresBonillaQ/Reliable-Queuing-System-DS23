package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.raft.impl.RaftLog;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.response.RaftLogEntryResponse;
import it.polimi.ds.network.gateway.server.ServerToGateway;
import it.polimi.ds.network.handler.BrokerRequestDispatcher;
import it.polimi.ds.network.utils.LeaderWaitingForFollowersCallable;
import it.polimi.ds.network.utils.LeaderWaitingForFollowersResponse;
import it.polimi.ds.network.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;
import it.polimi.ds.utils.config.Timing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(LeaderBrokerState.class.getName());
    private final AtomicBoolean hasAlreadyNotifyGateway = new AtomicBoolean(false);
    private final Set<String> brokerIdSetVotedOk = new ConcurrentSkipListSet<>();

    public LeaderBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        log.log(Level.INFO, "I'm becoming leader!!");
        startHeartBeat();
        brokerContext.getBrokerRaftIntegration().increaseCurrentTerm();
        ExecutorInstance.getInstance().getExecutorService().submit(new ServerToGateway(brokerContext, brokerContext.getMyBrokerConfig().getBrokerServerPortToGateway()));
        //ExecutorInstance.getInstance().getExecutorService().submit(new ClientToGateway(brokerContext, brokerContext.getMyBrokerConfig().getGatewayInfo()));
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
        log.log(Level.INFO, "Request from gateway: {0}", requestLine);

        // append log and take it
        final List<RaftLog> raftLog = brokerContext.getBrokerRaftIntegration().buildAndAppendNewLog(requestLine);

        //build logEntryRequest to forward to all followers
        final RequestMessage raftLogMessage = NetworkMessageBuilder.Request.buildAppendEntryLogRequest(
                brokerContext.getBrokerRaftIntegration().getCurrentTerm(),
                brokerContext.getMyBrokerConfig().getMyBrokerId(),
                brokerContext.getBrokerRaftIntegration().getPrevLogIndex(),
                brokerContext.getBrokerRaftIntegration().getPrevLogTerm(brokerContext.getBrokerRaftIntegration().getPrevLogIndex()),
                raftLog
        );

        log.log(Level.INFO, "Forwarding to all followers {0}", raftLogMessage);

        // passing message to each thread which handle client connection with followers
        ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(GsonInstance.getInstance().getGson().toJson(raftLogMessage));

        // start from 1 because the leader vote OK for himself
        int numConsensus = 1;
        final int numOtherBrokersAlive = ThreadsCommunication.getInstance().getNumThreadsOfAliveBrokers();
        brokerIdSetVotedOk.clear();

        if(numOtherBrokersAlive > 0){

            // creating callables waiting for followers responses
            ExecutorService executorService = Executors.newCachedThreadPool();
            CompletionService<LeaderWaitingForFollowersResponse> completionService = new ExecutorCompletionService<>(executorService);

            ThreadsCommunication.getInstance()
                    .getResponseConcurrentHashMap()
                    .forEach((queueId, responseQueue) -> {
                        completionService.submit(new LeaderWaitingForFollowersCallable(queueId, responseQueue));
                    });

            // consuming followers responses
            for (int i = 0; i < numOtherBrokersAlive; i++) {

                try{
                    Future<LeaderWaitingForFollowersResponse> response = completionService.take();

                    final String brokerIdOfResponse = response.get().getBrokerId();

                    log.log(Level.INFO, "Response from follower taken from queue: {0}", response.get());

                    ResponseMessage responseMessage = GsonInstance.getInstance().getGson().fromJson(response.get().getResponse(), ResponseMessage.class);

                    if(ResponseIdEnum.APPEND_ENTRY_LOG_RESPONSE.equals(responseMessage.getId())) {

                        RaftLogEntryResponse raftLogEntryResponse = GsonInstance.getInstance().getGson().fromJson(responseMessage.getContent(), RaftLogEntryResponse.class);

                        if (StatusEnum.OK.equals(raftLogEntryResponse.getStatus())) {

                            log.log(Level.INFO, "Consensus +1");
                            brokerIdSetVotedOk.add(brokerIdOfResponse);
                            numConsensus++;

                        } else {

                            //build logEntryRequest to forward to KO followers
                            final int myLogSize = brokerContext.getBrokerRaftIntegration().getLogQueueSize();
                            int numAttemps = 0;

                            while(numAttemps < myLogSize){

                                final RequestMessage requestMessage = NetworkMessageBuilder.Request.buildAppendEntryLogRequest(
                                        brokerContext.getBrokerRaftIntegration().getCurrentTerm(),
                                        brokerContext.getMyBrokerConfig().getMyBrokerId(),
                                        brokerContext.getBrokerRaftIntegration().getPrevLogIndexOf(raftLogEntryResponse.getLastMatchIndex()),
                                        brokerContext.getBrokerRaftIntegration().getPrevLogTermOfIndex(raftLogEntryResponse.getLastMatchIndex()),
                                        brokerContext.getBrokerRaftIntegration().getRaftLogEntriesFromIndex(raftLogEntryResponse.getLastMatchIndex())
                                );

                                log.log(Level.INFO, "AppendLogEntryRequest new to solve is {0}", requestMessage);

                                brokerContext.getBrokerRaftIntegration().printLogs();

                                ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(brokerIdOfResponse).add(GsonInstance.getInstance().getGson().toJson(requestMessage));

                                Future<LeaderWaitingForFollowersResponse> responseCallableRetry = executorService.submit(
                                        new LeaderWaitingForFollowersCallable(
                                                response.get().getBrokerId(),
                                                ThreadsCommunication.getInstance().getResponseConcurrentHashMapOfBrokerId(brokerIdOfResponse)
                                        )
                                );

                                ResponseMessage responseMessageCallableRetry = GsonInstance.getInstance().getGson().fromJson(responseCallableRetry.get().getResponse(), ResponseMessage.class);
                                log.log(Level.INFO, "AppendLogEntryResponse of the new to solve is {0}", responseMessageCallableRetry);

                                if(ResponseIdEnum.APPEND_ENTRY_LOG_RESPONSE.equals(responseMessageCallableRetry.getId())){
                                    raftLogEntryResponse = GsonInstance.getInstance().getGson().fromJson(responseMessageCallableRetry.getContent(), RaftLogEntryResponse.class);

                                    if (StatusEnum.OK.equals(raftLogEntryResponse.getStatus())) {

                                        log.log(Level.INFO, "Consensus +1");
                                        brokerIdSetVotedOk.add(brokerIdOfResponse);
                                        numConsensus++;
                                        break;

                                    }
                                }

                                numAttemps++;

                            }

                        }
                    }

                } catch (InterruptedException e){
                    log.log(Level.SEVERE, "Error: {0}", e.getMessage());
                } catch (ExecutionException e) {
                    log.log(Level.INFO, "Error: {0}", e.getMessage());
                } catch (Exception e) {
                    log.log(Level.INFO, "Error: {0}", e.getMessage());
                }
            }

        }

        // considering case of cluster with only one broker
        if(
                (numOtherBrokersAlive > 0 && numConsensus >= Math.floorDiv(brokerContext.getNumClusterBrokers(), 2) + 1) ||
                        numOtherBrokersAlive == 0
        ){

            log.log(Level.INFO, "Consensus reached, executing command and responding to gateway..");

            try {

                // exec the request locally
                ResponseMessage response = BrokerRequestDispatcher.exec(brokerContext, requestLine);
                brokerContext.getBrokerModel().printState();

                // send commit to gateway
                out.println(GsonInstance.getInstance().getGson().toJson(response));
                out.flush();

            } catch (RequestNoManagedException e) {
                log.log(Level.SEVERE, "Request {} not managed, review logic because already checked!!!", requestLine);
            }

            // increase my lastCommitIndex
            brokerContext.getBrokerRaftIntegration().increaseLastCommitIndex();

            // TODO Aggiungere logica per settare a TRUE il campo committed dei log! (c'è bisogno del campo?)

            // send commit to all followers that voted ok!
            RequestMessage requestMessage = NetworkMessageBuilder.Request.buildCommitRequest(brokerContext.getBrokerRaftIntegration().getLastCommitIndex());
            brokerIdSetVotedOk.forEach(x -> ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(x).add(GsonInstance.getInstance().getGson().toJson(requestMessage)));
        }
    }

    /**
     * This method handles all responses received from followers
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

    }

    /**
     * This method handle logic to send HeartBeat to followers
     * */
    private void startHeartBeat(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {

                    if(!brokerContext.isBrokerSetUp())
                        return;

                    RequestMessage requestMessage = NetworkMessageBuilder.Request.buildHeartBeatRequest(brokerContext.getMyBrokerConfig().getMyBrokerId());

                    ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(
                            GsonInstance.getInstance().getGson().toJson(requestMessage)
                    );

                }, Timing.HEARTBEAT_DELAY_SENDING, Timing.HEARTBEAT_PERIOD_SENDING, TimeUnit.MILLISECONDS
        );
    }
}
