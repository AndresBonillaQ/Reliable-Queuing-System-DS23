package it.polimi.ds.network.broker.client;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.exception.network.ImpossibleSetUpException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.response.SetUpResponse;
import it.polimi.ds.network.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;
import it.polimi.ds.utils.config.BrokerInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientToBroker implements Runnable {

    private final Logger log = Logger.getLogger(ClientToBroker.class.getName());
    private final BrokerInfo brokerInfo;
    private final BrokerContext brokerContext;
    private final ScheduledExecutorService scheduler;

    public ClientToBroker(BrokerInfo brokerInfo, BrokerContext brokerContext) {
        this.brokerInfo = brokerInfo;
        this.brokerContext = brokerContext;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run() {
        connectToBroker();
    }

    private void connectToBroker() {
        scheduler.schedule(() -> {
            try (
                    Socket socket = new Socket(brokerInfo.getHostName(), brokerInfo.getPort());
                    InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                    BufferedReader in = new BufferedReader(streamReader);
                    PrintWriter out = new PrintWriter(socket.getOutputStream())
            ) {

                socket.setSoTimeout(500);
                sendFirstSetupMessage(in, out);
                log.log(Level.INFO, "Client connection to BrokerID {0} set-upped", brokerInfo.getClientBrokerId());

                while (socket.isConnected() && !socket.isClosed()) {
                    try{
                        brokerContext.getBrokerState().clientToBrokerExec(brokerInfo.getClientBrokerId(), in, out);
                    } catch (SocketTimeoutException ignored){}
                }

                log.log(Level.INFO, "Client connection with {0} closed, trying to reconnect", brokerInfo.getClientBrokerId());
                retryReconnection();

            } catch (ImpossibleSetUpException ex){
                log.log(Level.SEVERE, "Impossible to setUp client connection to the broker {0}, error: {1}, closing broker..", new Object[]{brokerInfo.getClientBrokerId(), ex.getMessage()});
            } catch (IOException ex) {
                //log.log(Level.SEVERE, "IOException in client connection to broker {0}, retrying connection..", brokerInfo.getClientBrokerId());
                retryReconnection();
            }

        }, 2, TimeUnit.SECONDS);
    }

    private void retryReconnection(){
        connectToBroker();
    }

    private void sendFirstSetupMessage(BufferedReader in, PrintWriter out) throws IOException, ImpossibleSetUpException {
        handleSetUpRequestToSend(out);
        handleSetUpResponse(in);
    }

    private void handleSetUpRequestToSend(PrintWriter out){
        RequestMessage requestMessage = NetworkMessageBuilder.Request.buildSetUpRequest(brokerContext.getMyBrokerConfig().getMyBrokerId());
        String request = GsonInstance.getInstance().getGson().toJson(requestMessage);

        out.println(request);
        out.flush();
    }

    private void handleSetUpResponse(BufferedReader in) throws IOException, ImpossibleSetUpException {
        String response = in.readLine();

        ResponseMessage responseMessage = GsonInstance.getInstance().getGson().fromJson(response, ResponseMessage.class);
        SetUpResponse setUpResponse = GsonInstance.getInstance().getGson().fromJson(responseMessage.getContent(), SetUpResponse.class);

        if (StatusEnum.KO.equals(setUpResponse.getStatus())) {
            log.log(Level.SEVERE, "Impossible to setUp client, error: {0}, finishing connection..", setUpResponse.getDesStatus());
            throw new ImpossibleSetUpException("Impossible to setUp with broker as client!");
        }
    }
}

