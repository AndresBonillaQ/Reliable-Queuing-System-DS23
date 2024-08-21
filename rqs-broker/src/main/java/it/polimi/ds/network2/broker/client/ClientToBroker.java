package it.polimi.ds.network2.broker.client;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.exception.network.ImpossibleSetUpException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.raft.response.SetUpResponse;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.network2.utils.thread.impl.ThreadsCommunication;
import it.polimi.ds.utils.config.BrokerInfo;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.NetworkMessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
            try {
                try (
                        Socket socket = new Socket(brokerInfo.getHostName(), brokerInfo.getPort());
                        InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                        BufferedReader in = new BufferedReader(streamReader);
                        PrintWriter out = new PrintWriter(socket.getOutputStream())
                ) {

                    log.log(Level.INFO, "Connected to BrokerID {0} on hostName: {1} and port {2}",
                            new Object[]{brokerInfo.getClientBrokerId(), brokerInfo.getHostName(), brokerInfo.getPort()});

                    sendFirstSetupMessage(in, out);

                    while (true) {
                        brokerContext.getBrokerState().clientToBrokerExec(brokerInfo.getClientBrokerId(), in, out);
                    }

                } catch (ImpossibleSetUpException ex){
                    log.log(Level.SEVERE, "Impossible to setUp the broker, error: {0}, closing broker..", ex.getMessage());
                } catch (IOException ex) {
                    log.log(Level.SEVERE, "Error {0}! IOException in clientToBroker connection, retrying connection..", ex.getMessage());
                    ThreadsCommunication.getInstance().onBrokerConnectionClose(brokerInfo.getClientBrokerId());
                    // Retry connection after a delay
                    connectToBroker();
                }

            } catch (Exception e) {
                log.severe("Unexpected error occurred: " + e.getMessage());
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void sendFirstSetupMessage(BufferedReader in, PrintWriter out) throws IOException, ImpossibleSetUpException {

        RequestMessage requestMessage = NetworkMessageBuilder.Request.buildSetUpRequest(brokerContext.getMyBrokerConfig().getMyBrokerId());
        String request = GsonInstance.getInstance().getGson().toJson(requestMessage);

        out.println(request);
        out.flush();

        String response = in.readLine();

        ResponseMessage responseMessage = GsonInstance.getInstance().getGson().fromJson(response, ResponseMessage.class);
        SetUpResponse setUpResponse = GsonInstance.getInstance().getGson().fromJson(responseMessage.getContent(), SetUpResponse.class);

        log.log(Level.INFO, "Setting up the client, received {0}", response);

        if (StatusEnum.KO.equals(setUpResponse.getStatus())) {
            log.log(Level.SEVERE, "Impossible to setUp client, error: {0}, finishing connection..", setUpResponse.getDesStatus());
            throw new ImpossibleSetUpException("Impossible to setUp with broker as client!");
        }
    }
}

