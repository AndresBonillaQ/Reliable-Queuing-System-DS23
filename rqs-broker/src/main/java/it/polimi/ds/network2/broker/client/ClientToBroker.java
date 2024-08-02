package it.polimi.ds.network2.broker.client;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.network2.utils.thread.impl.ThreadsCommunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientToBroker implements Runnable{

    private final Logger log = Logger.getLogger(ClientToBroker.class.getName());
    private final InetSocketAddress brokerAddress;
    private final BrokerContext brokerContext;

    public ClientToBroker(InetSocketAddress inetSocketAddress, BrokerContext brokerContext){
        this.brokerAddress = inetSocketAddress;
        this.brokerContext = brokerContext;
    }

    @Override
    public void run() {

        try {
            log.log(Level.INFO, "WAITING 15 sec to make the other server going up...");
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            return;
        }

        try(
                Socket socket = new Socket(brokerAddress.getHostName(), brokerAddress.getPort());
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader in = new BufferedReader(streamReader);
                PrintWriter out = new PrintWriter(socket.getOutputStream());
        ){

            log.log(Level.INFO, "Connection as client established with port {0}", socket.getPort());

            while(true){
                try{
                    brokerContext.getBrokerState().clientToBrokerExec(socket, in, out);
                }catch (IOException e){
                    log.log(Level.INFO, "ERROR IOException in communication with broker as client: {0}", e.getMessage());
                    return;
                }
            }

        } catch (IOException ex) {
            log.severe("Error! IOException in clientToBroker connection");
            log.severe(ex.getMessage());
        }
    }
}
