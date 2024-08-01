package it.polimi.ds.old.network.leader.toGateway;

import it.polimi.ds.old.broker.BrokerContext;
import it.polimi.ds.old.network.leader.toGateway.connectionHandler.LeaderGatewayHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class LeaderToGateway{

    private final int leaderPort;
    private final BlockingQueue<String> requestBlockingQueue;
    private final BrokerContext brokerContext;

    private final Logger log = Logger.getLogger(LeaderToGateway.class.getName());

    public LeaderToGateway(BrokerContext brokerContext, int leaderPort, BlockingQueue<String> requestBlockingQueue){
        this.leaderPort = leaderPort;
        this.requestBlockingQueue = requestBlockingQueue;
        this.brokerContext = brokerContext;
    }

    /**
     * Leader allow gateway connections, actuating as server
     * */
    public void start(ExecutorService executor) {
        try(
                ServerSocket serverSocket = new ServerSocket(this.leaderPort);
        ){

            Socket socket;

            while(true){
                try{
                    socket = serverSocket.accept();
                    log.severe("New gateway is connected!");
                }catch (IOException e){
                    log.severe("IO Exception during gateway connection");
                    return;
                }

                Thread thread = new Thread(new LeaderGatewayHandler(brokerContext, socket, requestBlockingQueue));
                executor.submit(thread);
            }

        } catch (IOException ex) {
            log.severe("Error! IOException");
            log.severe(ex.getMessage());
        }
    }
}
