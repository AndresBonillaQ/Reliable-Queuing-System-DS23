package it.polimi.ds.old.network.leader.toGateway.connectionHandler;

import it.polimi.ds.old.broker.BrokerContext;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.old.network.leader.toGateway.dispatcher.LeaderRequestDispatcher;
import it.polimi.ds.utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class LeaderGatewayHandler implements Runnable {

    private final Socket clientSocket;
    private final Logger log = Logger.getLogger(LeaderGatewayHandler.class.getName());
    private final BlockingQueue<String> requestBlockingQueue;
    private final BrokerContext brokerContext;

    public LeaderGatewayHandler(BrokerContext brokerContext, Socket clientSocket, BlockingQueue<String> requestBlockingQueue){
        this.clientSocket = clientSocket;
        this.requestBlockingQueue = requestBlockingQueue;
        this.brokerContext = brokerContext;
    }

    @Override
    public void run() {
        try(
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ){

            String request;
            ResponseMessage response;

            while(true){

                try{
                    //Receive request from socket
                    request = in.readLine();
                }catch (IOException e){
                    log.severe("Io exception reading from gateway socket!");
                    return;
                }

                try{
                    //Process request
                    response = LeaderRequestDispatcher.exec(brokerContext.getBrokerModel(), request);

                    //passing request to each follower thread
                    requestBlockingQueue.put(request);

                    //wait to dt until all thread toFollower send request to followers and response

                    //check consensus

                    //if consensus send COMMIT_MSG to followers and OK to gateway

                    //if no consensus no send ABORT(?) to followers and KO to gateway

                } catch (RequestNoManagedException e){
                    log.severe("Request " + request + " not managed!");
                    break;
                } catch (InterruptedException e) {
                    log.severe("Interrupted exception throw by put in blockingQueue, closing gateway connection!");
                    Thread.currentThread().interrupt();
                    return;
                }

                //Send response to socket
                writer.println(GsonInstance.getInstance().getGson().toJson(response));
                writer.flush();
            }

        } catch (IOException ex) {
            log.severe("Error! IOException");
            log.severe(ex.getMessage());
        }
    }
}
