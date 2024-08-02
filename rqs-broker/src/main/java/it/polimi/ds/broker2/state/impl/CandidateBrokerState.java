package it.polimi.ds.broker2.state.impl;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.broker2.state.BrokerState;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class CandidateBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(CandidateBrokerState.class.getName());

    public CandidateBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
    }

    @Override
    public void clientToBrokerExec(Socket socket, BufferedReader in, PrintWriter out) {
        log.info("clientToBrokerExec: IT's candidate..");
        //deny each message
    }

    @Override
    public void clientToDnsExec(BufferedReader in, PrintWriter out) {
        log.info("clientToDnsExec: IT's candidate..");
        //deny each message
    }

    @Override
    public void serverToGatewayExec(BufferedReader in, PrintWriter out) {
        log.info("serverToGatewayExec: IT's candidate..");
        //deny ALL messages
    }

    @Override
    public void serverToBrokerExec(Socket socket, BufferedReader in, PrintWriter out) {
        log.info("serverToBrokerExec: IT's candidate..");
        //deny each message
    }

    @Override
    public void onWinLeaderElection() {
        brokerContext.setBrokerState(new LeaderBrokerState(brokerContext));
    }

    @Override
    public void onLoseLeaderElection() {
        brokerContext.setBrokerState(new FollowerBrokerState(brokerContext));
    }
}
