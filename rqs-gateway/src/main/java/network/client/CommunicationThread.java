package network.client;

import network.server.model.GateWay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

public class CommunicationThread extends Thread {
    private Socket socket;
    private ConnectionListener listener;
    private final String clusterID;


    public CommunicationThread(Socket socket, ConnectionListener listener, String clusterID) {
        this.socket = socket;
        this.listener = listener;
        this.clusterID = clusterID;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                if (GateWay.getInstance().fetchRequest(clusterID) != null) { // prende la queue delle richieste relative al cluster con ID = clusterID
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(Objects.requireNonNull(GateWay.getInstance().fetchRequest(clusterID)).getBytes());
                    outputStream.flush();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder jsonData = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) { //legge il messaggio formato json
                    jsonData.append(line);
                }
                GateWay.getInstance().addToResponseQueue(jsonData);

            } catch (IOException e) {
                if (listener != null) {
                    listener.onConnectionLost(clusterID);
                }
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}