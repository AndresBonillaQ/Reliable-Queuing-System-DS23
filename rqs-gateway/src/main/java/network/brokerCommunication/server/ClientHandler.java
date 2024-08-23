package network.brokerCommunication.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import messages.MessageResponse;
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
        System.out.println("Client connected...");
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
                        MessageResponse messageResponse = gson.fromJson(inputLine, MessageResponse.class);
                        synchronized (Gateway.getInstance()) {
                            Gateway.getInstance().processResponse(messageResponse);
                        }
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
