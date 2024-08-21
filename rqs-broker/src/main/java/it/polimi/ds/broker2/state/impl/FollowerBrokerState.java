package it.polimi.ds.broker2.state.impl;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.broker2.state.BrokerState;
import it.polimi.ds.message.election.RequestDispatcher;
import it.polimi.ds.utils.ExecutorInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FollowerBrokerState extends BrokerState {

    private final Object heartBeatTimerThreadLock = new Object();
    private final Logger log = Logger.getLogger(FollowerBrokerState.class.getName());
    private AtomicBoolean heartBeatReceived = new AtomicBoolean(false);
    private boolean voted;


    public FollowerBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        voted = false;
        heartbeatTimerThreadStart();
    }

    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) {
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
     * Followers accept messages only from leader and candidate
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();

        if(requestLine != null && !requestLine.isEmpty()){
            log.log(Level.INFO, "Request from Leader: {0} ; responding to brokerId {1}", new Object[]{requestLine, clientBrokerId});
            out.println(RequestDispatcher.processRequest(requestLine,this.getBrokerContext().getBrokerRaftIntegration().getCurrentTerm(), this));
            out.flush();
            //Da togliere
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
        synchronized (heartBeatTimerThreadLock) {
            //follower becomes candidate
            brokerContext.setBrokerState(new CandidateBrokerState(brokerContext));
           // brokerContext.getBrokerState().getStatusLock().notify();
          //  heartBeatTimerThreadLock.notify();
        }
    }

    private void heartbeatTimerThreadStart(){
        //submitta ad uno dei 100 thread nel pool (qual'ora ce ne sia uno libero) il task
        ExecutorInstance.getInstance().getExecutorService().submit(
                () -> {
                    synchronized (heartBeatTimerThreadLock) { //to let this thread become the owner of the monitor
                        while (true) {
                            try {
                                System.out.println("HEARTBEAT sleeping...");
                                heartBeatTimerThreadLock.wait(3000 + (int) (Math.random() * 3000)); //wait a random time between 3 sec and 6 sec
                                if (!heartBeatReceived.get())
                                    onHeartbeatTimeout();
                                else
                                    heartBeatReceived.set(false);
                                //return;
                            } catch (InterruptedException e) {
                                 log.info("HeartBeat received! Restarting timer.");
                            }
                        }
                    }
                }
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
