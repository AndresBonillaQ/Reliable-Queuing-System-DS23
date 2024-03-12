package it.polimi.ds.network2.dns;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.network2.broker.client.ClientToBroker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientToDns implements Runnable{
    private final Logger log = Logger.getLogger(ClientToBroker.class.getName());
    private final InetSocketAddress brokerAddress;
    private final BrokerContext brokerContext;

    public ClientToDns(InetSocketAddress inetSocketAddress, BrokerContext brokerContext){
        this.brokerAddress = inetSocketAddress;
        this.brokerContext = brokerContext;
    }

    @Override
    public void run() {
        try(
                Socket socket = new Socket(brokerAddress.getHostName(), brokerAddress.getPort());
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader in = new BufferedReader(streamReader);
                PrintWriter out = new PrintWriter(socket.getOutputStream());
        ){

            while(true){
                brokerContext.getBrokerState().clientToDnsExec(in, out);
            }

        } catch (IOException ex) {
            log.severe("Error! IOException in clientToBroker connection");
            log.severe(ex.getMessage());
        }
    }
}
