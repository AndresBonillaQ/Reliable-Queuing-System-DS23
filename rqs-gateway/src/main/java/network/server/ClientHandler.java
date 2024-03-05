package network.server;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

import network.server.model.GateWay;


/**
 * per ogni client che richiede la connessione col server viene spawnato un thread che lo "gestisce"
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    public ClientHandler(Socket socket) throws IOException {

        this.clientSocket = socket;
        System.out.println("Client connected...");
    }
    @Override
    public void run() {
        while (!clientSocket.isClosed()) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                StringBuilder jsonData = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) { //legge il messaggio formato json
                    jsonData.append(line);
                }
                String clientID;
                synchronized (GateWay.getInstance()) {
                    clientID = GateWay.getInstance().processRequest(jsonData);
                }

                if (!GateWay.getInstance().fetchResponse(clientID).isEmpty()) {
                    OutputStream outputStream = clientSocket.getOutputStream();
                    outputStream.write(Objects.requireNonNull(GateWay.getInstance().fetchResponse(clientID).poll()).getBytes());
                    outputStream.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}