package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.election.responses.VoteResponse;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.request.HeartbeatRequest;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;
import it.polimi.ds.utils.config.Timing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CandidateBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(CandidateBrokerState.class.getName());
    private final AtomicBoolean hasAlreadySentVoteRequest = new AtomicBoolean(false);
    private final AtomicInteger numVotesReceived;

    private final ScheduledFuture<?> electionTimeoutTask;

    public CandidateBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        log.log(Level.INFO, "I'm candidate!");

        brokerContext.getBrokerRaftIntegration().increaseCurrentTerm();
        numVotesReceived = new AtomicInteger(1);

        electionTimeoutTask = startElectionTimeout();
    }

    /**
     * Method to send RequestVote to followers
     * */
    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {
        if(!hasAlreadySentVoteRequest.get()){
            hasAlreadySentVoteRequest.set(true);
            sendVoteRequest(clientBrokerId, out);
            receiveVoteResponse(clientBrokerId, in, out);
        }
    }

    /**
     * Method to handle messages from brokers
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();
        handleServerToBrokerMessage(clientBrokerId, requestLine, out);
    }

    private void handleServerToBrokerMessage(String clientBrokerId, String requestLine, PrintWriter out){

        log.log(Level.INFO, "Candidate Received from broker {0} : {1}", new Object[]{clientBrokerId, requestLine});
        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);

        switch (requestMessage.getId()){
            case VOTE_REQUEST -> handleVoteRequestFromBroker(requestMessage, out);
            case HEARTBEAT_REQUEST -> handleHeartBeatRequestFromBroker(requestMessage, out);
            default -> log.log(Level.INFO, "Candidate Request not managed: {0}", requestLine);
        }
    }

    private void handleVoteRequestFromBroker(RequestMessage requestMessage, PrintWriter out){
        VoteRequest voteRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), VoteRequest.class);
        log.log(Level.INFO, "VoteRequest received during my candidate with term {0}, voting KO!!!", voteRequest.getTerm());

        ResponseMessage responseMessage;

        if(voteRequest.getTerm() > brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
            log.log(Level.INFO, "Received a vote request with higher term, i'm losing election..");
            responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.OK, "");
            numVotesReceived.decrementAndGet(); //removing vote to myself
        } else
            responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "Already voted for myself!");

        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();
    }

    private void handleHeartBeatRequestFromBroker(RequestMessage requestMessage, PrintWriter out){
        HeartbeatRequest heartbeatRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), HeartbeatRequest.class);
        log.log(Level.INFO, "HeartBeat of leader {0} received! Another Candidate has become Leader before me!!!", heartbeatRequest.getLeaderId());

        ResponseMessage responseMessage;
        if(heartbeatRequest.getTerm() >= brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
            log.log(Level.INFO, "Received a heartbeat while i was candidate with higher term, i'm losing election..");
            responseMessage = NetworkMessageBuilder.Response.buildHeartBeatResponse(StatusEnum.OK, "");
            brokerContext.updateNewLeaderInfo(heartbeatRequest.getLeaderId());
            onLoseLeaderElection();
        } else
            responseMessage = NetworkMessageBuilder.Response.buildHeartBeatResponse(StatusEnum.KO, "My currentTerm is greater, you are not the leader!");

        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();
    }

    /**
     * No NEW_LEADER_REQUEST to followers because of HeartBeat
     * */
    @Override
    public void onWinLeaderElection() {
        electionTimeoutTask.cancel(true);
        synchronized (brokerContext.getBrokerState()){
            brokerContext.setBrokerState(new LeaderBrokerState(brokerContext));
        }
    }

    @Override
    public void onLoseLeaderElection() {
        log.log(Level.INFO, "Losing election..");
        electionTimeoutTask.cancel(true);
        synchronized (brokerContext.getBrokerState()){
            brokerContext.setBrokerState(new FollowerBrokerState(brokerContext));
        }
    }

    private void sendVoteRequest(String clientBrokerId, PrintWriter out) {
        RequestMessage requestVoteMessage = NetworkMessageBuilder.Request.buildVoteRequest(brokerContext.getBrokerRaftIntegration().getCurrentTerm());
        log.log(Level.INFO, "Forwarding request to followers {0} : {1}", new Object[]{clientBrokerId, requestVoteMessage});
        out.println(GsonInstance.getInstance().getGson().toJson(requestVoteMessage));
        out.flush();
    }

    private void receiveVoteResponse(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {
        String message = in.readLine();
        log.log(Level.INFO, "VoteResponse from {0} is {1}", new Object[]{clientBrokerId, message});

        ResponseMessage responseMessage = GsonInstance.getInstance().getGson().fromJson(message, ResponseMessage.class);

        if (responseMessage.getId().equals(ResponseIdEnum.VOTE_RESPONSE)) {
            VoteResponse voteResponse = GsonInstance.getInstance().getGson().fromJson(responseMessage.getContent(), VoteResponse.class);

            if(StatusEnum.OK.equals(voteResponse.getStatus()))
                numVotesReceived.incrementAndGet();
        }
    }

    private ScheduledFuture<?> startElectionTimeout(){
        return Executors.newSingleThreadScheduledExecutor().schedule(
                this::checkElectionOutCome,
                Timing.ELECTION_TIMEOUT,
                TimeUnit.MILLISECONDS
        );
    }

    private void checkElectionOutCome(){
        log.log(Level.INFO, "Candidate timeout, checking result!");
        if(numVotesReceived.get() > Math.floorDiv(brokerContext.getNumClusterBrokers(), 2))
            onWinLeaderElection();
        else
            onLoseLeaderElection();
    }
}
