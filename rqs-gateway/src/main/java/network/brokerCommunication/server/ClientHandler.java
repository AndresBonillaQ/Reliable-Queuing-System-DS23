package network.brokerCommunication.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.polimi.ds.network.gateway.client.ClientToGateway;
import messages.MessageRequest;
import messages.connectionSetUp.SetUpConnectionMessage;
import messages.id.RequestIdEnum;
import model.Gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class acts as server for the broker (the new leader); each time a new leader is elected it sends a message to the gateway
 * so it knows who the new leader is.
 */

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private String clientID;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
    }
    private final Logger log = Logger.getLogger(ClientHandler.class.getName());


    @Override
    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Gson gson = new Gson();

       while (!clientSocket.isClosed()) {

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                try {
                    MessageRequest requestMessage = gson.fromJson(inputLine, MessageRequest.class);

                    if (RequestIdEnum.NEW_LEADER_TO_GATEWAY_REQUEST.equals(requestMessage.getId())) {
                        SetUpConnectionMessage setUpConnectionMessage = gson.fromJson(requestMessage.getContent(), SetUpConnectionMessage.class);
                        synchronized (Gateway.getInstance()) {
                            Gateway.getInstance().setUpConnectionWithNewLeader(setUpConnectionMessage);
                        }
                        clientSocket.close();
                    } else
                        System.out.println("Not managed");
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
           }
        }
            } catch (IOException e) {
            log.log(Level.SEVERE, "Error {0}! IOException in clientToGateway connection, retrying connection..", e.getMessage());
        }finally {
                try {
                    if (!clientSocket.isClosed()) {
                        clientSocket.close();
                    }
                    System.out.println("The leader has been recognized, closing this socket ");

                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
