package it.polimi.ds.network2.broker.server.handler;

import it.polimi.ds.broker2.BrokerContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class BrokerHandler implements Runnable{

    private final Socket socket;
    private final BrokerContext brokerContext;
    private final Logger log = Logger.getLogger(BrokerHandler.class.getName());

    public BrokerHandler(BrokerContext brokerContext, Socket socket){
        this.socket = socket;
        this.brokerContext = brokerContext;
    }

    @Override
    public void run() {
        try(
                PrintWriter out = new PrintWriter(socket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
           ){

            log.info("Starting communication with broker!");

            while(true){
                try{
                    brokerContext.getBrokerState().serverToBrokerExec(in, out);
                } catch (IOException e){
                    log.severe("IOException in communication with broker!");
                    return;
                }
            }

        }catch (IOException ex){
            log.severe("IO Exception in connection with broker server!");
        }
    }
}
