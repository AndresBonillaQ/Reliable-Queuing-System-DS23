package it.polimi.ds.network2.gateway;

import it.polimi.ds.broker2.BrokerContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerToGateway implements Runnable {

    private final Logger log = Logger.getLogger(ServerToGateway.class.getName());
    private final BrokerContext brokerContext;

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
            try(
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    ){

                while(true){
                    synchronized (brokerContext.getBrokerState()){
                        try {
                            brokerContext.getBrokerState().serverToGatewayExec(in, out);
                        } catch (IOException e) {
                            log.severe("Error! IOException during communication with gateway!");
                            log.severe(e.getMessage());
                            return;
                        }
                    }
                }


            } catch (IOException e) {
                log.severe("Error! IOException during establishing connection with gateway!");
                log.severe(e.getMessage());
            }

        } catch (IOException e) {
            log.severe("Error! IOException during creation of server socket to gateway!");
            log.severe(e.getMessage());
        }
    }
}
