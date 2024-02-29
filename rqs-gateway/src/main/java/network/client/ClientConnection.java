package network.client;

import network.DNScommunication.HandleDNS;
import network.server.model.GateWay;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientConnection {

    private HashMap<String, Socket> socketHashMap = new HashMap<>();

    /**
     * apre la connessione tra il gateway e tutti i clients
     */
    public void openConnection() throws IOException {
        for (String clusterId : GateWay.getInstance().getClusterID()
        ) {
            try {
                Socket socket = new Socket(GateWay.getInstance().getIp(clusterId), GateWay.getInstance().getPortNumber(clusterId));
                socketHashMap.put(clusterId, socket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
