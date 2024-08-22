package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.raft.impl.RaftLog;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.election.responses.VoteResponse;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.DesStatusEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.request.CommitLogRequest;
import it.polimi.ds.message.raft.request.HeartbeatRequest;
import it.polimi.ds.message.raft.request.RaftLogEntryRequest;
import it.polimi.ds.network.handler.BrokerRequestDispatcher;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.NetworkMessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FollowerBrokerState extends BrokerState {

    private final Object heartBeatTimerThreadLock = new Object();
    private final Logger log = Logger.getLogger(FollowerBrokerState.class.getName());
    private final AtomicBoolean heartBeatReceived = new AtomicBoolean(false);
    private boolean voted;
    private Long random = 5500 + (long) (Math.random() * 15000);

    public FollowerBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        voted = false;
        heartbeatTimerThreadStart();
        System.out.println("random: " + random);
    }

    /**
     * This method is not used as Follower but as Leader
     * Waiting to become a Leader
     * */
    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws InterruptedException {

    }

    @Override
    public void clientToGatewayExec(BufferedReader in, PrintWriter out) {

    }

    @Override
    public void serverToGatewayExec(BufferedReader in, PrintWriter out) throws IOException {
        // request not managed..
    }

    /**
     * Followers accept request from leader and requestVote from candidate
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();

        if(requestLine != null && !requestLine.isEmpty()){
            log.log(Level.INFO, "Request from Leader: {0} ; responding to brokerId {1}", new Object[]{requestLine, clientBrokerId});

            RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);
            ResponseMessage responseMessage;

            switch (requestMessage.getId()) {
                case APPEND_ENTRY_LOG_REQUEST -> {

                    RaftLogEntryRequest raftLogEntryRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), RaftLogEntryRequest.class);

                    // only if is leader and log is consistency
                    if (Objects.equals(brokerContext.getLeaderId(), raftLogEntryRequest.getLeaderId())) {

                        if (brokerContext.getBrokerRaftIntegration().isConsistent(raftLogEntryRequest.getPrevLogIndex(), raftLogEntryRequest.getPrevLogTerm())) {

                            brokerContext.getBrokerRaftIntegration().appendLog(raftLogEntryRequest.getRafLogEntries());

                            responseMessage = NetworkMessageBuilder.Response.buildAppendEntryLogResponse(
                                    StatusEnum.OK,
                                    DesStatusEnum.RAFT_LOG_ENTRY_OK.getValue(),
                                    -1
                            );

                        } else {

                            log.log(Level.INFO, "For request, broker is leader, log is NOT consistent!");

                            responseMessage = NetworkMessageBuilder.Response.buildAppendEntryLogResponse(
                                    StatusEnum.KO,
                                    DesStatusEnum.RAFT_LOG_ENTRY_KO.getValue(),
                                    raftLogEntryRequest.getPrevLogIndex()
                            );

                        }
                    } else {
                        // bad request no leader
                        log.log(Level.INFO, "For request, broker is NOT leader");
                        responseMessage = new ResponseMessage();
                    }

                    //forwarding message
                    out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                    out.flush();
                }

                // no response
                case COMMIT_REQUEST -> {
                    log.log(Level.INFO, "COMMIT OPERATION!!!");

                    CommitLogRequest commitLogRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), CommitLogRequest.class);
                    List<RaftLog> raftLogList = brokerContext.getBrokerRaftIntegration().getLogsToCommit(commitLogRequest.getLastCommitIndex());
                    System.out.println("RaftLostList size: " + raftLogList.size());
                    brokerContext.getBrokerRaftIntegration().increaseLastCommitIndex(commitLogRequest.getLastCommitIndex());
                    System.out.println("Last commit index: " + brokerContext.getBrokerRaftIntegration().getLastCommitIndex());

                    raftLogList.forEach(raftLog -> {
                        try {
                            raftLog.setCommitted(true);
                            BrokerRequestDispatcher.exec(brokerContext, raftLog.getRequest());
                            brokerContext.getBrokerModel().printState();
                        } catch (RequestNoManagedException e) {
                            log.log(Level.INFO, "Request {0} cannot be executed due to error: {1}", new Object[]{raftLog.getRequest(), e.getMessage()});
                            throw new RuntimeException(e);
                        }
                    });

                }

                case HEARTBEAT_REQUEST -> {
                    HeartbeatRequest heartbeatRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), HeartbeatRequest.class);
                    log.log(Level.INFO, "HeartBeat of leader {0} received!", heartbeatRequest.getLeaderId());

                    brokerContext.updateNewLeaderInfo(heartbeatRequest.getLeaderId());

                    ResponseMessage responseMessage1 = NetworkMessageBuilder.Response.buildHeartBeatResponse();
                    out.println(GsonInstance.getInstance().getGson().toJson(responseMessage1));
                    out.flush();

                    heartBeatReceived.set(true);
                }

                case VOTE_REQUEST -> {

                    VoteRequest voteRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), VoteRequest.class);
                    log.log(Level.INFO, "VoteRequest is: {0}", requestLine);

                    VoteResponse voteResponse = new VoteResponse();
                    voteResponse.setOutcome("OK");

                    ResponseMessage responseMessage1 = new ResponseMessage();
                    responseMessage1.setId(ResponseIdEnum.VOTE_OUTCOME);
                    responseMessage1.setContent(GsonInstance.getInstance().getGson().toJson(voteResponse));

                    out.println(GsonInstance.getInstance().getGson().toJson(responseMessage1));
                    out.flush();
                }

                default -> {
                    //not managed
                    System.out.println("DEFAULT");
                }
            }
        }

        //heartBeatTimerThreadLock.notify();
/*
        ResponseMessage responseMessage = new ResponseMessage(
                ResponseIdEnum.HEARTBEAT_RESPONSE,
                GsonInstance.getInstance().getGson().toJson(new HeartbeatResponse())
        );*/

    }

    public void setHeartBeatReceived(boolean value) {
        heartBeatReceived.set(value);
    }
    @Override
    public void onHeartbeatTimeout(){
        System.out.println("Becoming a candidate..");
        //follower becomes candidate
        brokerContext.setBrokerState(new CandidateBrokerState(brokerContext));
        // brokerContext.getBrokerState().getStatusLock().notify();
        //  heartBeatTimerThreadLock.notify();
    }

    private void heartbeatTimerThreadStart(){
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
                () -> {

                    if (scheduler.isShutdown()) {
                        return;
                    }

                    log.log(Level.INFO, "heartBeatReceived is {0}", heartBeatReceived);
                    if(!heartBeatReceived.get()) {
                        onHeartbeatTimeout();
                        scheduler.shutdownNow();
                    }
                    else
                        heartBeatReceived.set(false);
                },
                5000,
                random,
                TimeUnit.MILLISECONDS
        );
    }

    public void notifyHeartbeatLock() {
        heartBeatReceived.set(true);
        this.heartBeatTimerThreadLock.notify();
    }
    public boolean isVoted() {
        return voted;
    }

    public void setVoted(boolean voted) {
        this.voted = voted;
    }
}
