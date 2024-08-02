package it.polimi.ds.network2.broker.server.handler;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.network2.utils.thread.impl.ThreadsCommunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
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
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader in = new BufferedReader(streamReader);
           ){
            ThreadsCommunication.getInstance().addSocketQueue(socket);

            while(true){
                try{
                    brokerContext.getBrokerState().serverToBrokerExec(socket, in, out);
                } catch (IOException e){
                    log.log(Level.SEVERE, "IOException in communication with broker client {0}!", socket.getPort());
                    return;
                }
            }

        }catch (IOException ex){
            log.log(Level.SEVERE, "IOException in connection with broker server!");
        }
    }
}
