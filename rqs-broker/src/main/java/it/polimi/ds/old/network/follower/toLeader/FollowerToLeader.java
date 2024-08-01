package it.polimi.ds.old.network.follower.toLeader;

import it.polimi.ds.old.broker.BrokerContext;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.exception.network.FollowerNoLeaderHeartbeatException;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.old.network.follower.toLeader.requestDispatcher.FollowerRequestDispatcher;
import it.polimi.ds.old.network.leader.toGateway.LeaderToGateway;
import it.polimi.ds.utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class FollowerToLeader implements Runnable{
    private final int followerPort;
    private final BrokerContext brokerContext;

    private final Logger log = Logger.getLogger(LeaderToGateway.class.getName());

    public FollowerToLeader(BrokerContext brokerContext, int followerPort){
        this.followerPort = followerPort;
        this.brokerContext = brokerContext;
    }

    /**
     * Follower allow one Leader connection!
     * */
    public void run() {
        try(
                ServerSocket serverSocket = new ServerSocket(this.followerPort);
        ){
            try(
                    Socket socket = serverSocket.accept();
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ){
                log.severe("Leader is connected!");

                String request;
                ResponseMessage response;

                while(true){

                    try{
                        //Receive request from socket
                        request = in.readLine();
                    }catch (IOException e){
                        log.severe("Io exception reading from leader socket!");
                        return;
                    }

                    //la request deve: essere messa sui log se request e rispondere con OK, quando arriva il messaggio di commit della request la si esegue
                    //bisonga duplicare i messaggi ricevuti e quindi anche il dispatcher sarà ad hoc
                    try{
                        FollowerRequestDispatcher.exec(brokerContext.getBrokerModel(), request);
                    } catch (RequestNoManagedException ex){
                        log.warning("Request (" + request + "), not managed!");
                    }

                    if(true)
                        throw new FollowerNoLeaderHeartbeatException();

                    //Send response to socket
                    writer.println(GsonInstance.getInstance().getGson().toJson(response));
                    writer.flush();
                }


            }catch (IOException e){
                log.severe("IO Exception during leader connection");
            }

        } catch (IOException ex) {
            log.severe("Error! IOException");
            log.severe(ex.getMessage());
        } catch (FollowerNoLeaderHeartbeatException ex){
            brokerContext.getBrokerState().onHeartbeatTimeout();
        }
    }
}
