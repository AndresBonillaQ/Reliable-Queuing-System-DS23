package network.brokerCommunication.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import messages.MessageRequest;
import messages.connectionSetUp.SetUpConnectionMessage;
import messages.id.RequestIdEnum;
import network.clientCommunication.model.Gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private String clientID;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        while (!clientSocket.isClosed()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Gson gson = new Gson();
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    try {
                        MessageRequest requestMessage = gson.fromJson(inputLine, MessageRequest.class);

                        if(RequestIdEnum.NEW_LEADER_TO_GATEWAY_REQUEST.equals(requestMessage.getId())){
                            SetUpConnectionMessage setUpConnectionMessage = gson.fromJson(requestMessage.getContent(), SetUpConnectionMessage.class);
                            synchronized (Gateway.getInstance()) {
                                Gateway.getInstance().setUpConnectionWithNewLeader(setUpConnectionMessage);
                            }
                            System.out.println("New leader connected");
                        } else
                            System.out.println("Not managed");
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
