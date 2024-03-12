package it.polimi.ds.broker2.state.impl;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.broker2.state.BrokerState;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.request.HeartbeatRequest;
import it.polimi.ds.message.request.utils.RequestIdEnum;
import it.polimi.ds.network2.utils.ThreadCommunication;
import it.polimi.ds.utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LeaderBrokerState extends BrokerState {

    private int consensus = 1;
    private boolean consensusReached = false;
    private final Object lock = new Object();

    private final Logger log = Logger.getLogger(LeaderBrokerState.class.getName());

    public LeaderBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        startHeartBeat();
    }

    @Override
    public void clientToBrokerExec(Socket socket, BufferedReader in, PrintWriter out) throws IOException {

        log.info("clientToBrokerExec: IT's leader");

        try{
            String requestToForward = ThreadCommunication.getInstance().getRequestConcurrentHashMap().get(socket).take();
            System.out.println("TAKE FROM QUEUE AND SENDING THE MESSAGE: { " + requestToForward + " }");
            out.println(requestToForward);
            out.flush();
        }catch (InterruptedException ex){
            return;
        }

    }

    @Override
    public void clientToDnsExec(BufferedReader in, PrintWriter out) {

    }

    @Override
    public void serverToGatewayExec(BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();

        ThreadCommunication.getInstance().getRequestConcurrentHashMap().values().forEach(
                blockingQueue -> {
                    try {
                        blockingQueue.put(requestLine);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        ThreadCommunication.getInstance().getRequestConcurrentHashMap().values().forEach(
                blockingQueue -> new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String response = blockingQueue.poll(1000, TimeUnit.MILLISECONDS);  //per ogni coda aspetto 1000 millesecondi max
                            if(!Objects.isNull(response)){
                                //arrivata la response da una coda, conosensus +1 se OK
                                synchronized (lock){
                                    if(!consensusReached){
                                        consensus++;

                                        if(consensus > 999){
                                            consensusReached = true;
                                        }
                                    }
                                }


                            } else {

                            }

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).start()
        );

    }

    @Override
    public void serverToBrokerExec(BufferedReader in, PrintWriter out) {
        log.info("serverToBrokerExec: IT's leader..");
    }

    private void resetConsensus(){
        consensus = 1;
    }

    private void startHeartBeat(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> ThreadCommunication.getInstance().getRequestConcurrentHashMap().values().forEach(queue -> {

                    System.out.println("HEARTBEAT WAKEUP, SENDING SIGNAL ");

                    RequestMessage requestMessage = new RequestMessage(
                            RequestIdEnum.HEARTBEAT_REQUEST,
                            GsonInstance.getInstance().getGson().toJson(new HeartbeatRequest())
                    );

                    try {
                        queue.put(GsonInstance.getInstance().getGson().toJson(requestMessage));
                    } catch (InterruptedException e) {
                        log.severe("ERROR! InterruptedExecution during PUT heartbeat on queue: " + queue);
                    }

                }), 15, 3, TimeUnit.SECONDS
        );
    }
}
