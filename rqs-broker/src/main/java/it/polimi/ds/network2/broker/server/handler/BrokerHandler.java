package it.polimi.ds.network2.broker.server.handler;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.exception.network.ImpossibleSetUpException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.request.SetUpRequest;
import it.polimi.ds.message.request.utils.RequestIdEnum;
import it.polimi.ds.message.response.SetUpResponse;
import it.polimi.ds.message.response.utils.DesStatusEnum;
import it.polimi.ds.message.response.utils.ResponseIdEnum;
import it.polimi.ds.message.response.utils.StatusEnum;
import it.polimi.ds.network2.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.GsonInstance;

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

            try{
                final String brokerClientId = receiveFirstSetupMessage(in, out);
                log.log(Level.INFO, "SetUp brokerClientId {0} finished !", brokerClientId);

                while(true){
                    brokerContext.getBrokerState().serverToBrokerExec(brokerClientId, in, out);
                }

            } catch (ImpossibleSetUpException | IOException e){
                log.log(Level.SEVERE, "ERROR in communication with broker, error: {0}", e.getMessage());
            }

        }catch (IOException ex){
            log.log(Level.SEVERE, "IOException in connection with broker server!");
        }
    }

    private String receiveFirstSetupMessage(BufferedReader in, PrintWriter out) throws IOException, ImpossibleSetUpException {

        ResponseMessage responseMessage;
        String msg;

        String request = in.readLine();

        RequestMessage requestMessage = GsonInstance.getInstance().getGson().fromJson(request, RequestMessage.class);

        if(RequestIdEnum.SET_UP_REQUEST.equals(requestMessage.getId())){

            SetUpRequest setUpRequest = GsonInstance.getInstance().getGson().fromJson(requestMessage.getContent(), SetUpRequest.class);
            final String brokerClientId = setUpRequest.getBrokerId();

            if(ThreadsCommunication.getInstance().addBrokerId(brokerClientId)){
                SetUpResponse setUpResponse = new SetUpResponse();
                setUpResponse.setStatus(StatusEnum.OK);
                setUpResponse.setDesStatus(DesStatusEnum.SET_UP_OK.getValue());

                responseMessage = new ResponseMessage(
                        ResponseIdEnum.SET_UP_RESPONSE,
                        GsonInstance.getInstance().getGson().toJson(setUpResponse)
                );

                out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                out.flush();

                return brokerClientId;

            } else {
                SetUpResponse setUpResponse = new SetUpResponse();
                setUpResponse.setStatus(StatusEnum.KO);
                setUpResponse.setDesStatus(DesStatusEnum.SET_UP_KO.getValue());

                responseMessage = new ResponseMessage(
                        ResponseIdEnum.SET_UP_RESPONSE,
                        GsonInstance.getInstance().getGson().toJson(setUpResponse)
                );

                out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                out.flush();
            }
        }

        throw new ImpossibleSetUpException("Impossible setUp client");
    }
}
