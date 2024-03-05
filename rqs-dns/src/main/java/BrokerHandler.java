import com.google.gson.Gson;
import messages.requests.NewIpMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class BrokerHandler extends Server {
    private HashMap<String, Integer> numberOfQueuePerCluster = new HashMap<>(); //< clusterID, numberOfQueues>

    public BrokerHandler(int portNumber) {
        super(portNumber);
    }

    public void handleClient(Socket clientSocket) {
        try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String brokerMessage;
            while ((brokerMessage = in.readLine()) != null) {
                processMessage(brokerMessage);
                out.println("ok");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void processMessage(String message) {
        Gson gson = new Gson();
        String clusterID = gson.fromJson(message, NewIpMessage.class).getClusterID();
        String ipAddress = gson.fromJson(message, NewIpMessage.class).getIpAddress();
        Dns.getInstance().setLeaderAddress(clusterID, ipAddress);
    }
}