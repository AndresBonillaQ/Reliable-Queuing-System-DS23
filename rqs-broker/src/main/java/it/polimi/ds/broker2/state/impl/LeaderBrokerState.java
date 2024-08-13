package it.polimi.ds.broker2.state.impl;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.broker2.state.BrokerState;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.raft.request.RaftLogEntryRequest;
import it.polimi.ds.message.request.HeartbeatRequest;
import it.polimi.ds.message.request.utils.RequestIdEnum;
import it.polimi.ds.network2.utils.LeaderWaitingForFollowersCallable;
import it.polimi.ds.network2.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.raftLog.RaftLog;
import it.polimi.ds.utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(LeaderBrokerState.class.getName());

    public LeaderBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        //startHeartBeat();
    }

    /**
     * This method handles all messages to forward to followers from the BlockingQueue
     * */
    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        try{
            //blocking execution until a message to forward is available
            String requestToForward = ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(clientBrokerId).poll(50, TimeUnit.MILLISECONDS);

            if(requestToForward != null && !requestToForward.isEmpty()){
                log.log(Level.INFO, "Forwarding request to follower: {0}", requestToForward);

                //forwarding message
                out.println(requestToForward);
                out.flush();

                String responseLine = in.readLine();
                log.log(Level.INFO, "RESPONSE: {0}", responseLine);

                // add message to responseQueue
                ThreadsCommunication.getInstance().addResponseToFollowerResponseQueue(clientBrokerId, responseLine);
            }

        }catch (InterruptedException e){
            log.log(Level.SEVERE, "ERROR: {0}", e.getMessage());
        }

    }

    @Override
    public void clientToDnsExec(BufferedReader in, PrintWriter out) {

    }

    /**
     * This method handles all messages coming from the gateway to the cluster Leader.
     * All messages follow these steps:
     *  - Message is appended in the leader log queue
     *  - Log is redirected to all followers
     *  - Wait for all followers OK
     *      - if consensus reached:
     *          - exec operation request
     *          - confirm followers to exec request
     *          - response OK to gateway
     *      - otherwhise:
     *          -
     * */
    @Override
    public void serverToGatewayExec(BufferedReader in, PrintWriter out) throws IOException {

        String requestLine = in.readLine();
        log.log(Level.INFO, "Request from gateway: {0}", requestLine);
/*
        if(BrokerRequestDispatcher.isRequestNotManaged(requestLine)){
            log.log(Level.SEVERE, "Request {} not managed!", requestLine);
            return;
        }*/

        ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(requestLine); //tmp

        //build log and append it
        final List<RaftLog> raftLog = getBrokerContext().getBrokerRaftIntegration().buildAndAppendNewLog(requestLine);

        //build logEntryRequest to forward to all followers
        final RaftLogEntryRequest raftLogEntryRequest = new RaftLogEntryRequest(
                getBrokerContext().getBrokerRaftIntegration().getCurrentTerm(),
                getBrokerContext().getMyBrokerConfig().getMyBrokerId(),
                getBrokerContext().getBrokerRaftIntegration().getPrevLogIndex(),
                getBrokerContext().getBrokerRaftIntegration().getPrevLogTerm(),
                -1, //TODO capire
                raftLog
        );

        final RequestMessage raftLogMessage = new RequestMessage(
                RequestIdEnum.APPEND_ENTRY_LOG_REQUEST,
                GsonInstance.getInstance().getGson().toJson(raftLogEntryRequest)
        );

        //pass message to each thread which handle client connection with followers
        ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(GsonInstance.getInstance().getGson().toJson(raftLogMessage));

        // start from 1 because the leader vote OK for himself
        final int numConsensus = 1;
        final int numFollowersThread = ThreadsCommunication.getInstance().getResponseConcurrentHashMap().size();

        // creo i callables dei thread che aspetteranno la response dei follower
        ExecutorService executorService = Executors.newFixedThreadPool(numFollowersThread);
        CompletionService<String> completionService = new ExecutorCompletionService<>(executorService);

        ThreadsCommunication.getInstance()
                .getResponseConcurrentHashMap()
                .values()
                .stream()
                .map(LeaderWaitingForFollowersCallable::new)
                .forEach(completionService::submit);

        // consumo i risultati dei callables
        for (int i = 0; i < numFollowersThread; i++) {
            //numConsensus += completionService.take().get();
/*
            if(numConsensus > 100) { //consenso raggiunto
                //SEND COMMIT MESSAGE
                //BrokerRequestDispatcher.exec(brokerContext.getBrokerModel(), requestLine);
            }*/

            try{
                Future<String> response = completionService.take();
                log.log(Level.INFO, "Response from follower taken from queue: {0}", response.get());
                out.println(response.get());
                out.flush();
            } catch (InterruptedException e){
                log.log(Level.SEVERE, "Error: {0}", e.getMessage());
            } catch (ExecutionException e) {
                log.log(Level.INFO, "Error: {0}", e.getMessage());
            }

        }
    }

    /**
     * This method handles all responses received from followers
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {
        //DENY all messages
    }

    private void startHeartBeat(){
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    RequestMessage requestMessage = new RequestMessage(
                            RequestIdEnum.HEARTBEAT_REQUEST,
                            GsonInstance.getInstance().getGson().toJson(new HeartbeatRequest())
                    );

                    ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(
                            GsonInstance.getInstance().getGson().toJson(requestMessage)
                    );

                }, 15, 3, TimeUnit.SECONDS
        );
    }
}
