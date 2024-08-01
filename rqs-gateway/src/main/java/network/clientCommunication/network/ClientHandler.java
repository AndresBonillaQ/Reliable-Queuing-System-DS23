package network.clientCommunication.network;

import java.io.*;
import java.net.Socket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import messages.MessageRequest;
import network.clientCommunication.model.Gateway;


/**
 * per ogni client che richiede la connessione col server viene spawnato un thread che lo "gestisce"
 */
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
                while ( ( inputLine =  reader.readLine() ) != null) {
                    try {
                        MessageRequest jsonData = gson.fromJson(inputLine, MessageRequest.class);
                        synchronized (Gateway.getInstance()) {
                            clientID = Gateway.getInstance().processRequest(jsonData);
                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                if (Gateway.getInstance().fetchResponse(clientID) != null) {
                    OutputStream outputStream = clientSocket.getOutputStream();
                    Gson gson1 = new Gson();
                    outputStream.write(gson1.toJson(Gateway.getInstance().fetchResponse(clientID) ).getBytes());
                    outputStream.flush();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}