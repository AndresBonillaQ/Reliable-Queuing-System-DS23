package network.brokerCommunication.client;

import com.google.gson.Gson;
import messages.MessageRequest;
import messages.MessageResponse;
import network.clientCommunication.model.Gateway;

import java.io.*;
import java.net.Socket;

public class CommunicationThread extends Thread {
    private Socket socket;
    private final ConnectionListener listener;
    private final String clusterID;
    MessageRequest messageRequest = new MessageRequest();
    Gson gson = new Gson();


    public CommunicationThread(Socket socket, ConnectionListener listener, String clusterID) {
        this.socket = socket;
        this.listener = listener;
        this.clusterID = clusterID;
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);

                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                //messaggio arrivato dal CLIENT da inviare al BROKER
                if ((messageRequest = Gateway.getInstance().pollRequest(this.clusterID)) != null) {
                    writer.println(gson.toJson(messageRequest));
                    writer.flush();

                    //risposta dal BROKER
                    String readLine = reader.readLine();
                    Gson gson1 = new Gson();
                    MessageResponse messageResponse = gson1.fromJson(readLine, MessageResponse.class);
                    String clientID = messageResponse.getClientID();
                    //inoltra la risposta del BROKER al CLIENT
                    Gateway.getInstance().putOnResponseMap(clientID, messageResponse);

                }

            } catch (IOException e) {
                if (listener != null) {
                    try {
                        Gateway.getInstance().setClusterAsDisconnected(clusterID);
                        synchronized (listener) {
                            listener.onConnectionLost(clusterID);
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
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