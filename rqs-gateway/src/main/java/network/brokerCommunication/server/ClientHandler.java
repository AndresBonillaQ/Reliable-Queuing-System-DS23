package network.brokerCommunication.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.utils.GsonInstance;
import messages.MessageRequest;
import messages.connectionSetUp.SetUpConnectionMessage;
import messages.id.RequestIdEnum;
import model.Gateway;
import messages.pingPong.PingPongMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class acts as server for the broker (the new leader); each time a new leader is elected it sends a message to the gateway
 * so it knows who the new leader is.
 */

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private String clientID;
    private AtomicBoolean pingPongReceived = new AtomicBoolean();
    private final AtomicBoolean hasBeenNotifiedByLeader = new AtomicBoolean(false);

    private Integer clusterId;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        pingPongReceived.set(true);
    }
    private final Logger log = Logger.getLogger(ClientHandler.class.getName());


    @Override
    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            Gson gson = new Gson();

            while (!clientSocket.isClosed()) {
                String inputLine;
                inputLine = reader.readLine();
                if(Boolean.FALSE.equals(hasBeenNotifiedByLeader.get())) {
                    MessageRequest requestMessage = gson.fromJson(inputLine, MessageRequest.class);
                    // if (RequestIdEnum.NEW_LEADER_TO_GATEWAY_REQUEST.equals(requestMessage.getId())) {
                    SetUpConnectionMessage setUpConnectionMessage = gson.fromJson(requestMessage.getContent(), SetUpConnectionMessage.class);
                    synchronized (Gateway.getInstance()) {
                        Gateway.getInstance().setUpConnectionWithNewLeader(setUpConnectionMessage);
                    }
                    clusterId = Integer.valueOf(setUpConnectionMessage.getClusterId());
                    writer.println(GsonInstance.getInstance().getGson().toJson(new PingPongMessage("OK")));
                    writer.flush();
                    hasBeenNotifiedByLeader.set(true);
                     //   clientSocket.close();
                    startPingPongTimer();
                } else {
                    PingPongMessage pingPongMessage = gson.fromJson(inputLine, PingPongMessage.class);
                    if (pingPongMessage.getStatus().equals("OK")) {
                        pingPongReceived.set(true);

                    //    System.out.println("SENDING PINGPONG");
                        writer.println(GsonInstance.getInstance().getGson().toJson(new PingPongMessage("OK")));
                        writer.flush();

                    }
                }
        }
            } catch (IOException e) {
            log.log(Level.SEVERE, "Error {0}! IOException in clientToGateway connection, retrying connection..", e.getMessage());
        }
    }



    private void startPingPongTimer() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(
                    () -> {
                        if (scheduler.isShutdown()) {
                            return;
                        }
                 //       log.log(Level.INFO, "Is leader alive {0}", pingPongReceived);
                        if(!pingPongReceived.get()) {
                            try {
                                onPingPongTimeOut();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            scheduler.shutdownNow();
                        }
                        else {
                            pingPongReceived.set(false);

                        }
                    },
                    6000,
                    500,
                    TimeUnit.MILLISECONDS
            );
        }
    private void onPingPongTimeOut() throws IOException {
        System.out.println("Ping pong not received from " + clusterId);
        Gateway.getInstance().getClusterToSocketMap().get(clusterId).close();
        Gateway.getInstance().removeFromRequestMap(clusterId);
    }
}
