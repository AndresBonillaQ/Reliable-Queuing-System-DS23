package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.raft.utils.RaftLog;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.election.requests.VoteRequest;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.pingPong.PingPongMessage;
import it.polimi.ds.message.raft.request.HeartbeatRequest;
import it.polimi.ds.network.gateway.client.ClientToGateway;
import it.polimi.ds.network.gateway.server.ServerToGateway;
import it.polimi.ds.network.handler.BrokerRequestDispatcher;
import it.polimi.ds.network.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;
import it.polimi.ds.utils.config.Timing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderBrokerState extends BrokerState {

    private final Logger log = Logger.getLogger(LeaderBrokerState.class.getName());
    private final AtomicBoolean hasAlreadyNotifyGateway = new AtomicBoolean(false);

    private final Thread serverToGatewayThread;
    private final Thread clientToGatewayThread;
    private final ScheduledFuture<?> heartbeatTask;

    public LeaderBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        log.log(Level.INFO, "I'm leader!!");

        serverToGatewayThread = new ServerToGateway(brokerContext, brokerContext.getMyBrokerConfig().getBrokerServerPortToGateway());
        clientToGatewayThread = new ClientToGateway(brokerContext, brokerContext.getMyBrokerConfig().getGatewayInfo());

        ExecutorInstance.getInstance().getExecutorService().submit(serverToGatewayThread);
        ExecutorInstance.getInstance().getExecutorService().submit(clientToGatewayThread);
        heartbeatTask = startHeartBeat();
    }

    /**
     * This method handles all messages to forward to followers from the BlockingQueue,
     * messages could be APPEND_LOG_ENTRY_REQUEST or COMMIT_REQUEST
     * */
    @Override
    public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {

        try{
            if(ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(clientBrokerId) != null){
                String requestToForward = ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(clientBrokerId).poll(10, TimeUnit.MILLISECONDS);
                handleClientToBrokerMessage(clientBrokerId, requestToForward, out, in);
            }

        }catch (InterruptedException e){
            log.log(Level.SEVERE, "ERROR: {0}", e.getMessage());
        }

    }

    /**
     * This method handles all responses received from followers
     * */
    @Override
    public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException {
        String requestLine = in.readLine();
        handleServerToBrokerMessage(out, requestLine);
    }

    private void handleClientToBrokerMessage(String clientBrokerId, String requestToForward, PrintWriter out, BufferedReader in) throws IOException {

        if(requestToForward != null && !requestToForward.isEmpty()){

            out.println(requestToForward);
            out.flush();

            handleClientToBrokerMessageResponse(clientBrokerId, requestToForward, in);
        }
    }

    private void handleClientToBrokerMessageResponse(String clientBrokerId, String requestToForwarded, BufferedReader in) throws IOException {
        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestToForwarded, RequestMessage.class);

        // Response of follower
        switch (requestMessage.getId()){

            case HEARTBEAT_REQUEST -> {
                String responseLine = in.readLine();
                log.log(Level.INFO,"HeartBeat response: {0}", responseLine);
            }

            case APPEND_ENTRY_LOG_REQUEST -> {
                String responseLine = in.readLine();
                ThreadsCommunication.getInstance().addResponseToFollowerResponseQueue(clientBrokerId, responseLine);
            }

            case COMMIT_REQUEST -> {} // No waiting for a response
        }
    }

    private void handleServerToBrokerMessage(PrintWriter out, String requestLine){

        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);

        switch (requestMessage.getId()){
            case VOTE_REQUEST -> handleVoteRequestFromBroker(out, requestMessage);
            case HEARTBEAT_REQUEST -> handleHeartBeatRequestFromBroker(out, requestMessage);
            default -> log.log(Level.INFO, "Leader Request not managed: {0}", requestLine);
        }
    }

    private void handleHeartBeatRequestFromBroker(PrintWriter out, RequestMessage requestMessage){
        HeartbeatRequest heartbeatRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), HeartbeatRequest.class);

        ResponseMessage responseMessage;
        if(heartbeatRequest.getTerm() <= brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
            log.log(Level.INFO, "An older leader is sending me heartbeats, I'm the leader!");
            responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "I'm the leader!");
        } else {
            log.log(Level.INFO, "An older leader is sending me heartbeats with higher term! my is {0}, received {1}", new Object[]{brokerContext.getBrokerRaftIntegration().getCurrentTerm(), heartbeatRequest.getTerm()});
            responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.OK, "");
        }

        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();

        onBecomeFollower();
    }

    private void handleVoteRequestFromBroker(PrintWriter out, RequestMessage requestMessage){
        VoteRequest voteRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), VoteRequest.class);

        ResponseMessage responseMessage;
        if(voteRequest.getTerm() <= brokerContext.getBrokerRaftIntegration().getCurrentTerm()){
            log.log(Level.INFO, "Someone tries to become a leader with VOTE_REQUEST but I'm the leader!");
            responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.KO, "I'm the leader!");
        } else {
            log.log(Level.INFO, "I am the leader but has received a VOTE_REQUEST with higher Term! my is {0}, received {1}", new Object[]{brokerContext.getBrokerRaftIntegration().getCurrentTerm(), voteRequest.getTerm()});
            responseMessage = NetworkMessageBuilder.Response.buildVoteResponse(StatusEnum.OK, "");
        }

        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();

        onBecomeFollower();
    }

    private void onBecomeFollower(){
        log.log(Level.INFO, "Becoming follower...");
        clientToGatewayThread.interrupt();
        serverToGatewayThread.interrupt();
        heartbeatTask.cancel(true);
        brokerContext.setBrokerState(new FollowerBrokerState(brokerContext));
    }

    /**
     * This method handle logic to send HeartBeat to followers
     * */
    private ScheduledFuture<?> startHeartBeat(){
        return Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                this::sendHeartBeat,
                0,  // when become leader all connections has been already set during follower state
                Timing.HEARTBEAT_PERIOD_SENDING,
                TimeUnit.MILLISECONDS
        );
    }

    private void sendHeartBeat(){
        RequestMessage requestMessage = NetworkMessageBuilder.Request.buildHeartBeatRequest(brokerContext.getMyBrokerConfig().getMyBrokerId(), brokerContext.getBrokerRaftIntegration().getCurrentTerm());
        log.log(Level.INFO, "Sending heartbeat to brokers: {0}", ThreadsCommunication.getInstance().getBrokerIds());
        ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(
                GsonInstance.getInstance().getGson().toJson(requestMessage)
        );
    }
}
