package network.server;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import messages.MessageRequest;
import network.server.model.GateWay;


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
                        String jsonString = gson.toJson(jsonData);
                        System.out.println(jsonString);
                        synchronized (GateWay.getInstance()) {
                            clientID = GateWay.getInstance().processRequest(jsonData);
                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                if (GateWay.getInstance().fetchResponse(clientID)!= null) {
                    OutputStream outputStream = clientSocket.getOutputStream();
                    outputStream.write(GateWay.getInstance().fetchResponse(clientID).getBytes());
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