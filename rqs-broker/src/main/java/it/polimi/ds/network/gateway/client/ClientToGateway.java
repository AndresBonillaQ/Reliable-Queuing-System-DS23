package it.polimi.ds.network.gateway.client;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.pingPong.PingPongMessage;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;
import it.polimi.ds.utils.config.GatewayInfo;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientToGateway extends Thread {

    private final BrokerContext brokerContext;
    private final GatewayInfo gatewayInfo;
    private final ScheduledExecutorService scheduler;
    private final Logger log = Logger.getLogger(ClientToGateway.class.getName());

    public ClientToGateway(BrokerContext brokerContext, GatewayInfo gatewayInfo){
        this.brokerContext = brokerContext;
        this.gatewayInfo = gatewayInfo;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void run() {
        connectToGateway();
    }

    private void connectToGateway() {
        scheduler.schedule(() -> {
            try {
                try (
                        Socket socket = new Socket(gatewayInfo.getHostName(), gatewayInfo.getPort());
                        PrintWriter out = new PrintWriter(socket.getOutputStream())
                ) {

                    sendSetUpMessage(out);

                } catch (IOException ex) {
                    //log.log(Level.SEVERE, "Error {0}! IOException in clientToGateway connection, retrying connection..", ex.getMessage());
                    connectToGateway();
                }

            } catch (Exception e) {
                log.severe("Unexpected error occurred: " + e.getMessage());
            }
        }, 5, TimeUnit.SECONDS);
    }

    private void sendSetUpMessage(PrintWriter out){
        RequestMessage requestMessage = NetworkMessageBuilder.Request.buildNewLeaderToGatewayRequest(
                brokerContext.getMyBrokerConfig().getMyClusterId(),
                brokerContext.getMyBrokerConfig().getMyBrokerId(),
                brokerContext.getMyBrokerConfig().getMyHostName(),
                brokerContext.getMyBrokerConfig().getBrokerServerPortToGateway()
        );

        log.log(Level.INFO, "Notifying gateway about new leader... {0}", requestMessage);

        out.println(GsonInstance.getInstance().getGson().toJson(requestMessage));
        out.flush();
    }
}
