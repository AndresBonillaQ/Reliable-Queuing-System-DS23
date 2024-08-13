package it.polimi.ds.network2.broker.client;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.exception.network.ImpossibleSetUpException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.request.SetUpRequest;
import it.polimi.ds.message.request.utils.RequestIdEnum;
import it.polimi.ds.message.response.SetUpResponse;
import it.polimi.ds.message.response.utils.StatusEnum;
import it.polimi.ds.network2.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.GsonInstance;

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
    private final String clientBrokerId;

    public ClientToBroker(InetSocketAddress inetSocketAddress, BrokerContext brokerContext, String clientBrokerId){
        this.brokerAddress = inetSocketAddress;
        this.brokerContext = brokerContext;
        this.clientBrokerId = clientBrokerId;
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
                Socket socket = new Socket(brokerAddress.getHostString(), brokerAddress.getPort());
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader in = new BufferedReader(streamReader);
                PrintWriter out = new PrintWriter(socket.getOutputStream());

        ){
            log.log(Level.INFO, "Connection as client established with host: {0} and port {1}", new Object[]{brokerAddress.getHostString(), brokerAddress.getPort()});

            try{
                sendFirstSetupMessage(in, out);

                while(true){
                    brokerContext.getBrokerState().clientToBrokerExec(clientBrokerId, in, out);
                }

            } catch (IOException | ImpossibleSetUpException e){
                log.log(Level.INFO, "ERROR IOException in communication with broker as client: {0}", e.getMessage());
            }

        } catch (IOException ex) {
            log.severe("Error! IOException in clientToBroker connection");
            log.severe(ex.getMessage());
        }
    }

    /**
     * First message sent to brokers is the ID
     * */
    private void sendFirstSetupMessage(BufferedReader in, PrintWriter out) throws IOException, ImpossibleSetUpException {
        SetUpRequest setUpRequest = new SetUpRequest(brokerContext.getBrokerId());

        RequestMessage requestMessage = new RequestMessage(
                RequestIdEnum.SET_UP_REQUEST,
                GsonInstance.getInstance().getGson().toJson(setUpRequest)
        );
        String request = GsonInstance.getInstance().getGson().toJson(requestMessage);

        out.println(request);
        out.flush();

        String response = in.readLine();
        SetUpResponse setUpResponse = GsonInstance.getInstance().getGson().fromJson(response, SetUpResponse.class);

        if(StatusEnum.KO.equals(setUpResponse.getStatus())){
            log.log(Level.SEVERE, "Impossible to setUp client, error: {0}, finishing connection..", setUpResponse.getDesStatus());
            throw new ImpossibleSetUpException("Impossible to setUp with broker as client!");
        }

        log.log(Level.INFO, "SetUp brokerClientId finished");
    }
}
