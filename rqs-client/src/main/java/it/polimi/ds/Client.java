package it.polimi.ds;

import it.polimi.ds.cli.CliHandler;
import it.polimi.ds.exception.CliExitException;
import it.polimi.ds.exception.ErrorSetUpException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.request.RequestIdEnum;
import it.polimi.ds.message.request.SetUpRequest;
import it.polimi.ds.message.response.ResponseIdEnum;
import it.polimi.ds.message.response.SetUpResponse;
import it.polimi.ds.message.response.StatusEnum;
import it.polimi.ds.network.dispatcher.ResponseDispatcher;
import it.polimi.ds.utils.config.GatewayConfig;
import it.polimi.ds.utils.GsonInstance;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
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
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
        ){
            RequestMessage request;

            setUpClient(writer, reader);

            while(true){

                try{
                    //Get message to send to gateway
                    request = CliHandler.getRequest(clientId);
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
        } catch (ErrorSetUpException ex){
            log.log(Level.SEVERE, "Killing client: {0}", ex.getMessage());
        }
    }

    private void setUpClient(PrintWriter write, BufferedReader reader) throws IOException, ErrorSetUpException {

        SetUpRequest setUpRequest = new SetUpRequest(clientId);
        RequestMessage requestMessage = new RequestMessage(
                RequestIdEnum.SET_UP_REQUEST,
                GsonInstance.getInstance().getGson().toJson(setUpRequest)
        );

        System.out.println("Sending to gateway " + requestMessage);
        write.println(GsonInstance.getInstance().getGson().toJson(requestMessage));
        write.flush();

        String readLine = reader.readLine();
        System.out.println("Response from gateway " + readLine);
        ResponseMessage responseMessage = GsonInstance.getInstance().getGson().fromJson(readLine, ResponseMessage.class);

        if(ResponseIdEnum.SET_UP_RESPONSE.equals(responseMessage.getId())){
            SetUpResponse setUpResponse = GsonInstance.getInstance().getGson().fromJson(responseMessage.getContent(), SetUpResponse.class);
            if(StatusEnum.OK.equals(setUpResponse.getStatus()))
                System.out.println("SetUp OK! with client " + setUpResponse.getClientId());
            else
                throw new ErrorSetUpException("Impossible to setUp client with gateway!");
        } else
            System.out.println("Not managed");

    }
}
