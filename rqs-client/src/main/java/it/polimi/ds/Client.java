package it.polimi.ds;

import it.polimi.ds.cli.CliHandler;
import it.polimi.ds.exception.CliExitException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.network.dispatcher.ResponseDispatcher;
import it.polimi.ds.utils.config.GatewayConfig;
import it.polimi.ds.utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

public class Client {
    private final String clientId;
    private final GatewayConfig gatewayConfig;
    private final Logger log = Logger.getLogger(Client.class.getName());

    public Client(GatewayConfig gatewayConfig, String clientId){
        this.gatewayConfig = gatewayConfig;
        this.clientId = clientId;
    }

    public void start(){
        try(
                Socket socket = new Socket(gatewayConfig.getIp(), gatewayConfig.getPort());
                Scanner in = new Scanner(System.in);
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
        ){
            RequestMessage request;
           // socket.setSoTimeout(gatewayConfig.getTimeout());

            while(true){

                try{
                    //Get message to send to gateway
                    request = CliHandler.getRequest(clientId, in);
                } catch (CliExitException e) {
                    log.info("Closing by user choice");
                    return;
                }

                //Send request to socket
                writer.println(GsonInstance.getInstance().getGson().toJson(request));
                writer.flush();

                //Receive response from gateway
                String response = reader.readLine();

                //process response
                ResponseDispatcher.exec(response);
            }

        } catch (IOException ex) {
            log.severe("Error! IOException");
            log.severe(ex.getMessage());
        }
    }
}
