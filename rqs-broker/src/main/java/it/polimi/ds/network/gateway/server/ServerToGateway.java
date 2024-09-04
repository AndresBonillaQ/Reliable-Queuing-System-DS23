package it.polimi.ds.network.gateway.server;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.raft.utils.RaftLog;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.network.handler.BrokerRequestDispatcher;
import it.polimi.ds.network.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerToGateway extends Thread {

    private final Logger log = Logger.getLogger(ServerToGateway.class.getName());
    private final BrokerContext brokerContext;
    private Set<String> brokerIdSetVotedOk = new ConcurrentSkipListSet<>();

    private final int serverPort;

    public ServerToGateway(BrokerContext brokerContext, int serverPort){
        this.brokerContext = brokerContext;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {

        try(
                ServerSocket serverSocket = new ServerSocket(serverPort)
        ){
            log.log(Level.INFO, "Broker open server to gateway on port : {0} !", serverPort);
            try(
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    ){

                log.log(Level.INFO, "Connection established with gateway!");

                while(clientSocket.isConnected() && !clientSocket.isClosed())
                    handleServerToGatewayConnection(in, out);

                log.log(Level.SEVERE, "serverToGatewayExec Connection closed");

            } catch (IOException e) {
                log.severe("Error! IOException during establishing connection with gateway!");
                log.severe(e.getMessage());
            }

        } catch (IOException e) {
            log.severe("Error! IOException during creation of server socket to gateway!");
            log.severe(e.getMessage());
        }
    }

    private void handleServerToGatewayConnection(BufferedReader in, PrintWriter out) throws IOException {
        String requestLine = in.readLine();

        forwardAppendLogRequestToAllFollowers(requestLine);
        brokerIdSetVotedOk = brokerContext.getBrokerRaftIntegration().calculateConsensus();
        handleConsensusOutcome(requestLine, out);

        brokerContext.getBrokerRaftIntegration().printLogs();
        brokerContext.getBrokerModel().printState();
    }

    private void forwardAppendLogRequestToAllFollowers(String requestLine){

        brokerContext.getBrokerRaftIntegration().buildAndAppendNewLog(requestLine);

        final List<RaftLog> raftLogList = brokerContext.getBrokerRaftIntegration().getLastLogAppended();
        final int prevLogIndex = brokerContext.getBrokerRaftIntegration().getPrevLogIndex();

        final RequestMessage raftLogMessage = NetworkMessageBuilder.Request.buildAppendEntryLogRequest(
                brokerContext.getBrokerRaftIntegration().getCurrentTerm(),
                brokerContext.getMyBrokerConfig().getMyBrokerId(),
                prevLogIndex,
                brokerContext.getBrokerRaftIntegration().getPrevLogTerm(prevLogIndex),
                raftLogList
        );

        log.log(Level.INFO, "Forwarding to all followers {0}", raftLogMessage);

        // passing message to each thread which handle client connection with followers
        ThreadsCommunication.getInstance().addRequestToAllFollowerRequestQueue(GsonInstance.getInstance().getGson().toJson(raftLogMessage));
    }

    private void handleConsensusOutcome(String requestLine, PrintWriter out){
        if(isConsensusReached())
            handleConsensusReached(requestLine, out);
        else
            handleConsensusNoReached(requestLine, out);
        brokerContext.persistLog();
    }

    private boolean isConsensusReached(){
        final int numFollowersAlive = ThreadsCommunication.getInstance().getNumThreadsOfAliveBrokers();
        final int numVotedOk = brokerIdSetVotedOk.size() + 1;   //+1 because voted for myself

        return (numFollowersAlive > 0 && numVotedOk >= Math.floorDiv(brokerContext.getNumClusterBrokers(), 2) + 1) ||
                (numFollowersAlive == 0 && brokerContext.getNumClusterBrokers() == 1);
    }

    private void handleConsensusReached(String requestLine, PrintWriter out){
        log.log(Level.INFO, "Consensus reached, executing command and responding to gateway..");

        // Exec uncommitted logs
        List<String> requestToExec = brokerContext.getBrokerRaftIntegration().getUncommittedLogsToExec();
        ResponseMessage response = new ResponseMessage();
        for (String s : requestToExec) {
            try {
                response = BrokerRequestDispatcher.exec(brokerContext, s);
            } catch (RequestNoManagedException e) {
                log.log(Level.SEVERE, "Request {} not managed!", requestLine);
            }
        }

        brokerContext.getBrokerRaftIntegration().updateLogCommitState();

        out.println(GsonInstance.getInstance().getGson().toJson(response));
        out.flush();

        // send commit msg to all followers that voted ok!
        log.log(Level.INFO, "Sending commit command to brokers {0}", ThreadsCommunication.getInstance().getBrokerIds());
        RequestMessage requestMessage = NetworkMessageBuilder.Request.buildCommitRequest(brokerContext.getBrokerRaftIntegration().getLastCommitIndex());
        brokerIdSetVotedOk.forEach(x -> ThreadsCommunication.getInstance().getRequestConcurrentHashMapOfBrokerId(x).add(GsonInstance.getInstance().getGson().toJson(requestMessage)));
    }

    private void handleConsensusNoReached(String requestLine, PrintWriter out){
        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(requestLine, RequestMessage.class);
        ResponseMessage responseMessage = NetworkMessageBuilder.Response.buildServiceUnavailableResponse(StatusEnum.KO, Const.ResponseDes.KO.UNAVAILABLE_SERVICE_KO, requestMessage.getClientId());
        log.log(Level.INFO, "Consensus Not reached, responding to gateway {0}", responseMessage);
        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();
    }
}
