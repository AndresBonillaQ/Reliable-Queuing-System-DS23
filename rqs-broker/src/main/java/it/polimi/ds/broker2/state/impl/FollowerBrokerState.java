package it.polimi.ds.broker2.state.impl;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.broker2.state.BrokerState;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.response.HeartbeatResponse;
import it.polimi.ds.message.response.utils.ResponseIdEnum;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FollowerBrokerState extends BrokerState {

    private final Object heartBeatTimerThreadLock = new Object();
    private final Logger log = Logger.getLogger(FollowerBrokerState.class.getName());

    public FollowerBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        //heartbeatTimerThreadStart();
    }

    @Override
    public void clientToBrokerExec(Socket socket, BufferedReader in, PrintWriter out) {
        //log.info("clientToBrokerExec: IT's follower");
        //deny each message
    }

    @Override
    public void clientToDnsExec(BufferedReader in, PrintWriter out) {
        //log.info("clientToDnsExec: IT's follower");
        //deny each message
    }

    @Override
    public void serverToGatewayExec(BufferedReader in, PrintWriter out) {
        //log.info("serverToGatewayExec: IT's follower");
        //deny ALL messages
    }

    /**
     * Followers accept messages only from leader
     * */
    @Override
    public void serverToBrokerExec(Socket socket, BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();

        if(requestLine != null && !requestLine.isEmpty()){
            log.log(Level.INFO, "Request from Leader: {0} ;responding to socket on port: {1}", new Object[]{requestLine, socket.getPort()});

            //forwarding message
            out.println("responseOf: " + requestLine);
            out.flush();
        }

        //heartBeatTimerThreadLock.notify();
/*
        ResponseMessage responseMessage = new ResponseMessage(
                ResponseIdEnum.HEARTBEAT_RESPONSE,
                GsonInstance.getInstance().getGson().toJson(new HeartbeatResponse())
        );*/


    }

    @Override
    public void onHeartbeatTimeout(){
        synchronized (brokerContext.getBrokerState()){
            brokerContext.setBrokerState(new CandidateBrokerState(brokerContext));
            brokerContext.getBrokerState().getStatusLock().notify();
        }
    }

    private void heartbeatTimerThreadStart(){
        ExecutorInstance.getInstance().getExecutorService().submit(
                () -> {
                    synchronized (heartBeatTimerThreadLock) {
                        while (true) {
                            try {
                                System.out.println("HEARTBEAT sleeping...");
                                wait((15+3)*1000); // Attende per 1 secondo
                                log.info("HeartBeat not received! I'm going to candidate myself..");
                                onHeartbeatTimeout();
                                return;
                            } catch (InterruptedException e) {
                                log.info("HeartBeat received! Restarting timer.");
                            }
                        }
                    }
                }
        );
    }
}
