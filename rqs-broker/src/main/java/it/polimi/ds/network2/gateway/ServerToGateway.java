package it.polimi.ds.network2.gateway;

import it.polimi.ds.broker2.BrokerContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
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
            log.log(Level.INFO, "Broker open server to gateway on port : {0} !", serverPort);
            try(
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    ){

                log.log(Level.INFO, "Connection established with gateway!");

                while(clientSocket.isConnected() && !clientSocket.isClosed()){
                    brokerContext.getBrokerState().serverToGatewayExec(in, out);
                }

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
}
