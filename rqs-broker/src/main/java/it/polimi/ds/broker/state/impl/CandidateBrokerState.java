package it.polimi.ds.broker.state.impl;

import com.google.gson.Gson;
import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.ElectionManager;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.election.responses.VoteResponse;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CandidateBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(CandidateBrokerState.class.getName());

    private boolean isElectionOnGoing;
    ElectionManager electionManager;

    public CandidateBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        electionManager = new ElectionManager(this.getBrokerContext());
        //electionStarted();
        isElectionOnGoing = true;
    }

    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {
        log.info("I'm candidate sending request vote..!");
        requestVote(out);
        //fetch the outcome of the vote
        String message = in.readLine();
        Gson gson = new Gson();

        System.out.println("Vote outcome is " + message);

        ResponseMessage responseMessage = gson.fromJson(message, ResponseMessage.class);
        if (responseMessage.getId().equals(ResponseIdEnum.VOTE_OUTCOME)) {
            fetchVote(out, responseMessage.getContent());
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

    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {
        //log.info("serverToBrokerExec: IT's candidate..");

        //Da togliere --> capire come Andrès gestisce come il leader/candidato ricevono le risposte dai followers

    }

    @Override
    public void onWinLeaderElection(PrintWriter out) {
        isElectionOnGoing = false;
        brokerContext.setBrokerState(new LeaderBrokerState(brokerContext));/*

        final RequestMessage imNewLeader = new RequestMessage(
                RequestIdEnum.NEW_LEADER_REQUEST,
                GsonInstance.getInstance().getGson().toJson(this.getBrokerContext().getBrokerRaftIntegration().getCurrentTerm() + 1)
        );
//        ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(GsonInstance.getInstance().getGson().toJson(imNewLeader));
        out.println(imNewLeader);
        log.log(Level.INFO, "Forwarding request to other followers: {0}", imNewLeader);*/
    }

    @Override
    public void onLoseLeaderElection() {
        brokerContext.setBrokerState(new FollowerBrokerState(brokerContext));
    }

    private void requestVote(PrintWriter out) throws IOException {
        if (isElectionOnGoing) {

            VoteRequest voteRequest = new VoteRequest(brokerContext.getBrokerRaftIntegration().getCurrentTerm() + 1);

            final RequestMessage requestVoteMessage = new RequestMessage(
                    RequestIdEnum.VOTE_REQUEST,
                    GsonInstance.getInstance().getGson().toJson(voteRequest)
            );

            this.getBrokerContext().getBrokerRaftIntegration().increaseCurrentTerm();

            //ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(GsonInstance.getInstance().getGson().toJson(requestVoteMessage));

            log.log(Level.INFO, "Forwarding request to other followers: {0}", requestVoteMessage);
            out.println(GsonInstance.getInstance().getGson().toJson(requestVoteMessage));
            out.flush();
        }

    }
    private int getRandomElectionTimeout() {
        return 150 + (int) (Math.random() * 150);
    }
    private void fetchVote(PrintWriter out, String message) throws IOException { //Do as server
        Gson gson = new Gson();
        VoteResponse voteResponse = gson.fromJson(message, VoteResponse.class);
        if (isElectionOnGoing && "OK".equals(voteResponse.getOutcome())) {
            this.electionManager.addVote();
            if (electionManager.isMajorityAchieved()) {
                System.out.println("I'm becoming leader!!");
                onWinLeaderElection(out);
            }
        }

    }

    private void electionStarted(){
        ExecutorInstance.getInstance().getExecutorService().submit(
                () -> {
                        while (isElectionOnGoing) {
                            try {
                                Thread.sleep(10000); //wait 10 sec
                                isElectionOnGoing = false; //termina l'elezione senza un leader
                                log.log(Level.INFO, "Election ended without a leader");
                                onLoseLeaderElection();
                                return;
                            } catch (InterruptedException e) {
                        }
                    }
                }
        );
    }
}
