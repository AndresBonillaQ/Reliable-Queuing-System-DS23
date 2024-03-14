package network.client;
import network.client.CommunicationThread;
import network.client.ConnectionListener;
import network.server.model.GateWay;

import java.io.IOException;
import java.net.Socket;

public class ConnectionManager implements ConnectionListener {
    public ConnectionManager() {
    }

    @Override
    public void onConnectionLost(String clusterID) {
        try {
            Socket newSocket = new Socket(GateWay.getInstance().getIp(clusterID), GateWay.getInstance().getPortNumber(clusterID));
            new CommunicationThread(newSocket, this,clusterID).start();
            System.out.println("Reconnected.");
        } catch (IOException e) {
            System.err.println("Reconnection failed.");
        }
    }
    public void startConnection() throws IOException {
        //apro la connessione con tutti i clusters
        for (String clusterId : GateWay.getInstance().getClusterID()
        ) {
            try {
                Socket socket = new Socket(GateWay.getInstance().getIp(clusterId), GateWay.getInstance().getPortNumber(clusterId));
                CommunicationThread communicationThread = new CommunicationThread(socket,this, clusterId);
                communicationThread.start();
                GateWay.getInstance().addToSocketMap(socket, clusterId);
                System.out.println(clusterId + GateWay.getInstance().getIp(clusterId) + GateWay.getInstance().getPortNumber(clusterId));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
