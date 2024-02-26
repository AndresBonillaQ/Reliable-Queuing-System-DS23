package network.client;

import network.DNScommunication.ConnectionToDNS;
import network.server.ClientHandler;
import network.server.GateWayServer;
import network.server.model.GateWay;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientConnection {
    private HashMap<String, String> clusterIdToAddressMap = new HashMap<String, String>(); //<clusterId, ip>
    private HashMap<String, String> queueToClusterIdMap = new HashMap<String, String>(); //<queueId, clusterId>
    private HashMap<String, Integer> clusterToPortMap = new HashMap<String, Integer>();
    private ArrayList<String> clusterIdList = new ArrayList<>();
    private HashMap<String, Socket> socketHashMap = new HashMap<>();


    /**
     * apre la connessione con tutti i clients
     */

    public void setAddress(String clusterId, String address) {
        clusterIdToAddressMap.put(clusterId, address);
    }

    public void setMap(String clusterId, int port) {
        clusterToPortMap.put(clusterId, port);
    }

    public void openConnection() {
        for (String clusterId : clusterIdList
        ) {
            try {
                Socket socket = new Socket(clusterIdToAddressMap.get(clusterId), clusterToPortMap.get(clusterId));
                socketHashMap.put(clusterId, socket);
                GateWay.getInstance().fillHashMaps(clusterId, clusterIdToAddressMap.get(clusterId), clusterToPortMap.get(clusterId), socket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
    private void fetchAddresses() {
        ConnectionToDNS connectionToDNS = new ConnectionToDNS();
        try {
            connectionToDNS.start();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
