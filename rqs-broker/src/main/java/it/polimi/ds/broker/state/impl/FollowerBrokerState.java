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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FollowerBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(FollowerBrokerState.class.getName());
    private final AtomicBoolean heartBeatReceived = new AtomicBoolean(false);
    private final Set<Integer> hasAlreadyVoteInTerm = new ConcurrentSkipListSet<>();
    private final Random random = new Random();

    private final ScheduledFuture<?> heartBeatTask;

    public FollowerBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        log.log(Level.INFO, "I'm follower");
        heartBeatTask = heartbeatTimerThreadStart();
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

    /**
     * Followers accept request from leader and requestVote from candidate
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();
        handleServerToBrokerMessage(clientBrokerId, requestLine, out);
    }

    private void handleServerToBrokerMessage(String clientBrokerId, String requestLine, PrintWriter out){

        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);
        switch (requestMessage.getId()) {
            case APPEND_ENTRY_LOG_REQUEST -> handleAppendLogEntryRequestFromBroker(clientBrokerId, requestMessage, out);
            case COMMIT_REQUEST -> handleCommitRequestFromBroker(clientBrokerId, requestMessage);   // no response
            case HEARTBEAT_REQUEST -> handleHeartBeatRequestFromBroker(requestMessage, out);
            case VOTE_REQUEST -> handleVoteRequestFromBroker(clientBrokerId, requestMessage, out);
            default -> log.log(Level.INFO, "Follower Request not managed: {0}", requestLine);
        }
    }

    private void handleVoteRequestFromBroker(String clientBrokerId, RequestMessage requestMessage, PrintWriter out){

        VoteRequest voteRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), VoteRequest.class);
        log.log(Level.INFO, "Vote Request received: {0}", voteRequest);
        ResponseMessage responseMessage;
        if(voteRequest.getTerm() > brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
            if(!hasAlreadyVoteInTerm.contains(voteRequest.getTerm())){
                log.log(Level.INFO, "Voting for candidate {0} in term {1}", new Object[]{clientBrokerId, voteRequest.getTerm()});
                hasAlreadyVoteInTerm.add(voteRequest.getTerm());
                responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.OK, "");
            } else {
                log.log(Level.INFO, "NO Voting for candidate {0} in term {1} because already voted another one", new Object[]{clientBrokerId, voteRequest.getTerm()});
                responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "Already voted for this term");
            }
        } else
            responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "CurrentTerm of follower is greater");


        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();
    }

    private void handleCommitRequestFromBroker(String clientBrokerId, RequestMessage requestMessage){
        log.log(Level.INFO, "Request from broker {0}: {1}", new Object[]{clientBrokerId, requestMessage});

        CommitLogRequest commitLogRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), CommitLogRequest.class);

        brokerContext.getBrokerRaftIntegration().processCommitRequestAndGetRequestsToExec(commitLogRequest.getLastCommitIndex())
                .forEach(request -> {
                    try {
                        BrokerRequestDispatcher.exec(brokerContext, request);
                    } catch (RequestNoManagedException e) {
                        log.log(Level.INFO, "Request {0} not managed", request);
                    }
                });

        System.out.println("After COMMIT_REQUEST");
        brokerContext.getBrokerRaftIntegration().printLogs();
    }

    private void handleHeartBeatRequestFromBroker(RequestMessage requestMessage, PrintWriter out){

        HeartbeatRequest heartbeatRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), HeartbeatRequest.class);
        log.log(Level.INFO, "HeartBeat received: {0}", heartbeatRequest);

        ResponseMessage responseMessage;
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

    private void handleAppendLogEntryRequestFromBroker(String clientBrokerId, RequestMessage requestMessage, PrintWriter out){

        log.log(Level.INFO, "Request from broker {0}: {1}", new Object[]{clientBrokerId, requestMessage});

        RaftLogEntryRequest raftLogEntryRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), RaftLogEntryRequest.class);
        ResponseMessage responseMessage;

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

    private ScheduledFuture<?> heartbeatTimerThreadStart(){
        //final long randomValue = Timing.HEARTBEAT_PERIOD_CHECKING_FROM + (long) (Math.random() * Timing.HEARTBEAT_PERIOD_CHECKING_FROM_TO_SUM);
        final long randomValue = random.nextInt(Timing.HEARTBEAT_PERIOD_CHECKING_FROM_TO_SUM - Timing.HEARTBEAT_PERIOD_CHECKING_FROM) + Timing.HEARTBEAT_PERIOD_CHECKING_FROM;
        final long delay = brokerContext.getHasChangeState().get() ? randomValue : Timing.HEARTBEAT_DELAY_CHECKING + randomValue;
        log.log(Level.INFO, "Start waiting heartbeat, hasChangeState: {0}, random: {1}, finalDelay: {2}", new Object[]{brokerContext.getHasChangeState(), randomValue, delay});

        return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                this::handleHeartBeatTimeoutTask,
                delay,
                randomValue,
                TimeUnit.MILLISECONDS
        );
    }

    private void handleHeartBeatTimeoutTask(){
        log.log(Level.INFO, "Is leader alive {0}", heartBeatReceived);
        if(!heartBeatReceived.get())
            onHeartbeatTimeout();
        else
            heartBeatReceived.set(false);
    }

    @Override
    public void onHeartbeatTimeout(){
        if(!hasAlreadyVoteInTerm.contains(brokerContext.getBrokerRaftIntegration().getCurrentTerm() + 1)){
            synchronized (brokerContext.getBrokerState()){
                log.log(Level.INFO, "I'm going to elect myself");
                heartBeatTask.cancel(true);
                brokerContext.setBrokerState(new CandidateBrokerState(brokerContext));
            }
        } else
            log.log(Level.INFO, "Timeout expired but another broker is Candidate in term {0}", brokerContext.getBrokerRaftIntegration().getCurrentTerm() + 1);
    }
}
