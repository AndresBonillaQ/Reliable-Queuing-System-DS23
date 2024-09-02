package it.polimi.ds.network.broker.server.handler;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.exception.network.ImpossibleSetUpException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.raft.request.SetUpRequest;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.message.model.response.utils.DesStatusEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.network.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

            String brokerClientId = null;
            socket.setSoTimeout(500);

            try{
                brokerClientId = receiveFirstSetupMessage(in, out);
                log.log(Level.INFO, "Broker {0} connected to the server and set-upped", brokerClientId);

                while(socket.isConnected() && !socket.isClosed()){
                    try{
                        brokerContext.getBrokerState().serverToBrokerExec(brokerClientId, in, out);
                    }catch (SocketTimeoutException ignored){}
                }

            } catch (IOException e){
                log.log(Level.SEVERE, "The connection with BrokerId {0} has closed, error: {1}", new Object[]{brokerClientId, e.getMessage()});
                ThreadsCommunication.getInstance().onBrokerConnectionClose(brokerClientId);
            } catch (ImpossibleSetUpException ex){
                log.log(Level.SEVERE, "Impossible to setUp the broker, error: {0}", ex.getMessage());
            }

        }catch (IOException ex){
            log.log(Level.SEVERE, "IOException in connection with broker server!");
        }
    }

    private String receiveFirstSetupMessage(BufferedReader in, PrintWriter out) throws IOException, ImpossibleSetUpException {

        ResponseMessage responseMessage;

        String request = in.readLine();
        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(request, RequestMessage.class);

        if(RequestIdEnum.SET_UP_REQUEST.equals(requestMessage.getId())){

            SetUpRequest setUpRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), SetUpRequest.class);
            final String brokerClientId = setUpRequest.getBrokerId();

            if(!ThreadsCommunication.getInstance().isBrokerIdPresent(brokerClientId)){

                ThreadsCommunication.getInstance().addBrokerId(brokerClientId);

                responseMessage = NetworkMessageBuilder.Response.buildSetUpResponse(StatusEnum.OK, DesStatusEnum.SET_UP_OK.getValue());
                out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                out.flush();

                return brokerClientId;

            } else {

                log.log(Level.INFO, "Impossible to register the brokerId {0}, already present", brokerClientId);
                responseMessage = NetworkMessageBuilder.Response.buildSetUpResponse(StatusEnum.KO, DesStatusEnum.SET_UP_KO.getValue());

                out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                out.flush();
            }
        }

        throw new ImpossibleSetUpException("Impossible setUp client");
    }
}
