package it.polimi.ds.network.gateway.client;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.utils.config.GatewayInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientToGateway implements Runnable {

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
                        InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                        BufferedReader in = new BufferedReader(streamReader);
                        PrintWriter out = new PrintWriter(socket.getOutputStream())
                ) {

                    log.log(Level.INFO, "Connected to gateway on hostName: {0} and port {1}",
                            new Object[]{gatewayInfo.getHostName(), gatewayInfo.getPort()});

                    while (socket.isConnected() && !socket.isClosed()) {
                        brokerContext.getBrokerState().clientToGatewayExec(in, out);
                    }

                } catch (IOException ex) {
                    log.log(Level.SEVERE, "Error {0}! IOException in clientToGateway connection, retrying connection..", ex.getMessage());
                    // Retry connection after a delay
                    connectToGateway();
                }

            } catch (Exception e) {
                log.severe("Unexpected error occurred: " + e.getMessage());
            }
        }, 5, TimeUnit.SECONDS);
    }
}
