package network.brokerCommunication.client;

import com.google.gson.Gson;
import messages.MessageRequest;
import messages.MessageResponse;
import model.Gateway;
import utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


/**
 * This class handles the communication with the leader of the cluster
 */
public class CommunicationThread extends Thread {
    private Socket socket;
    private final ConnectionListener listener;
    private final Integer clusterID;
    MessageRequest messageRequest = new MessageRequest();
    Gson gson = new Gson();


    public CommunicationThread(Socket socket, ConnectionListener listener, Integer clusterID) {
        this.socket = socket;
        this.listener = listener;
        this.clusterID = clusterID;
    }

    @Override
    public void run() {

        try {
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            while (!socket.isClosed()) {

                //messaggio arrivato dal CLIENT da inviare al BROKER
                synchronized (Gateway.getInstance()) {
                    if ((messageRequest = Gateway.getInstance().pollRequest(this.clusterID)) != null) {

                        System.out.println("Message request to leader: " + messageRequest);

                        writer.println(gson.toJson(messageRequest));
                        writer.flush();

                        //risposta dal BROKER
                        String readLine = reader.readLine();

                        System.out.println("Response from the leader: " + readLine);

                        MessageResponse messageResponse = GsonInstance.getInstance().getGson().fromJson(readLine, MessageResponse.class);

                        String clientID = messageResponse.getClientId();

                        //inoltra la risposta del BROKER al CLIENT
                        Gateway.getInstance().putOnResponseMap(clientID, messageResponse);
                    }
                }
            }

        } catch (IOException e) {

            Gateway.getInstance().setClusterAsDisconnected(clusterID);
            Gateway.getInstance().removeFromRequestMap(clusterID);

            if (listener != null) {
                try {
                    synchronized (listener) {
                        listener.onConnectionLost(clusterID);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }


    }
}