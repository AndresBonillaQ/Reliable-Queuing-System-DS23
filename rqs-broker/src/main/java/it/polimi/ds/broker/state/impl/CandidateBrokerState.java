package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.election.ElectionManager;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.election.responses.VoteResponse;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.request.HeartbeatRequest;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CandidateBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(CandidateBrokerState.class.getName());

    private final ElectionManager electionManager = new ElectionManager(brokerContext);

    private final Set<String> hasAlreadySetRequestVote;

    public CandidateBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        brokerContext.getBrokerRaftIntegration().increaseCurrentTerm();
        hasAlreadySetRequestVote = new ConcurrentSkipListSet<>();
    }

    /**
     * Method to send RequestVote to followers
     * */
    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        if(!hasAlreadySetRequestVote.contains(clientBrokerId)){
            hasAlreadySetRequestVote.add(clientBrokerId);
            sendVoteRequest(clientBrokerId, out);
            receiveVoteResponse(clientBrokerId, in, out);
        }
    }

    @Override
    public void clientToGatewayExec(BufferedReader in, PrintWriter out) {
        //log.info("clientToDnsExec: IT's candidate..");
        //deny each message
    }

    @Override
    public void serverToGatewayExec(BufferedReader in, PrintWriter out) {
        //log.info("serverToGatewayExec: IT's candidate..");
        //deny ALL messages
    }

    /**
     * Method to handle messages from brokers
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();

        if(requestLine != null && !requestLine.isEmpty()){
            log.log(Level.INFO, "Candidate Received from broker {0} : {1}", new Object[]{clientBrokerId, requestLine});

            RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);

            switch (requestMessage.getId()){

                case VOTE_REQUEST -> {
                    VoteRequest voteRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), VoteRequest.class);
                    log.log(Level.INFO, "VoteRequest received during my candidate with term {0}, voting KO!!!", voteRequest.getTerm());

                    ResponseMessage responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "Already voted for myself!");

                    out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                    out.flush();
                }

                case HEARTBEAT_REQUEST -> {
                    HeartbeatRequest heartbeatRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), HeartbeatRequest.class);
                    log.log(Level.INFO, "HeartBeat of leader {0} received! Another Candidate has become Leader before me!!!", heartbeatRequest.getLeaderId());

                    ResponseMessage responseMessage;
                    if(heartbeatRequest.getTerm() >= brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
                        responseMessage = NetworkMessageBuilder.Response.buildHeartBeatResponse(StatusEnum.OK, "");
                        brokerContext.updateNewLeaderInfo(heartbeatRequest.getLeaderId());
                        onLoseLeaderElection();
                    } else
                        responseMessage = NetworkMessageBuilder.Response.buildHeartBeatResponse(StatusEnum.KO, "My currentTerm is greater, you are not the leader!");

                    out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                    out.flush();
                }

                default -> log.log(Level.INFO, "AS CANDIDATE REQUEST NOT MANAGED: {0}", requestLine);
            }
        }
    }

    /**
     * No NEW_LEADER_REQUEST to followers because of HeartBeat
     * */
    @Override
    public void onWinLeaderElection(PrintWriter out) {
        synchronized (brokerContext.getBrokerState()){
            brokerContext.setBrokerState(new LeaderBrokerState(brokerContext));
        }
    }

    @Override
    public void onLoseLeaderElection() {
        log.log(Level.INFO, "Losing election..");
        brokerContext.setBrokerState(new FollowerBrokerState(brokerContext));
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
                electionManager.addVote();

            if (electionManager.isMajorityAchieved())
                onWinLeaderElection(out);
            else if (hasAlreadySetRequestVote.size() == brokerContext.getNumClusterBrokers() - 1)
                onLoseLeaderElection();
        }
    }
}
