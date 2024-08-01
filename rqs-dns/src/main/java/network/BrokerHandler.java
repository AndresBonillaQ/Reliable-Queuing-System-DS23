package network;

import com.google.gson.Gson;
import messages.requests.NewIpMessage;
import model.Dns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BrokerHandler extends Server {
    public BrokerHandler(int portNumber) {
        super(portNumber);
    }

    public void handleClient(Socket clientSocket) {
        while (!clientSocket.isClosed()) {
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
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void processMessage(String message) {
        Gson gson = new Gson();
        NewIpMessage newIpMessage = gson.fromJson(message, NewIpMessage.class);
        String clusterID = newIpMessage.getClusterID();
        String ipAddress = newIpMessage.getIpAddress();
        Dns.getInstance().setLeaderAddress(clusterID, ipAddress);
    }
}