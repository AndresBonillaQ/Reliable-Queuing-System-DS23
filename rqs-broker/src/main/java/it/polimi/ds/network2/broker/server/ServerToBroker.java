package it.polimi.ds.network2.broker.server;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.network2.broker.server.handler.BrokerHandler;
import it.polimi.ds.utils.ExecutorInstance;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Principalmente per ricevere voti dei candidati e per ricevere ordini dal leader
 * */
public class ServerToBroker implements Runnable{

    private final Logger log = Logger.getLogger(ServerToBroker.class.getName());
    private final BrokerContext brokerContext;

    private final int serverPort;

    public ServerToBroker(BrokerContext brokerContext, int serverPort){
        this.brokerContext = brokerContext;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try(
                ServerSocket serverSocket = new ServerSocket(serverPort);
        ){

            log.log(Level.INFO, "Broker has open as server on port: {0} !", serverSocket.getLocalPort());

            while(true){
                try{
                    Socket clientSocket = serverSocket.accept();
                    log.log(Level.INFO, "A new broker has been connected as client on port {0}!", clientSocket.getPort());
                    ExecutorInstance.getInstance().getExecutorService().submit(new BrokerHandler(brokerContext, clientSocket));
                }catch (IOException ex){
                    log.log(Level.INFO, "ERROR opening connection with broker as client!");
                    return;
                }
            }

        } catch (IOException ex) {
            log.log(Level.INFO, "Error! IOException opening server to brokers");
            log.severe(ex.getMessage());
        }
    }
}
