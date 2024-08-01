package network.brokerCommunication.client;
import network.clientCommunication.model.Gateway;

import java.io.IOException;
import java.net.Socket;

public  class ConnectionManager implements ConnectionListener {

    public ConnectionManager() {
    }

    @Override
    public void onConnectionLost(String clusterID) throws IOException {

        while (!Gateway.getInstance().isUpdated(clusterID))
            try {
                Socket newSocket = new Socket(Gateway.getInstance().getIp(clusterID), Gateway.getInstance().getPortNumber(clusterID));
                new CommunicationThread(newSocket,
                        this,clusterID).start();
                System.out.println("Reconnected.");
            } catch (IOException e) {
                System.err.println("Reconnection failed.");
            }
    }
    public void startConnection() throws IOException {
        //apro la connessione con tutti i clusters
        for (String clusterId : Gateway.getInstance().getClustersID()
        ) {
            try {
                Socket socket = new Socket(Gateway.getInstance().getIp(clusterId), Gateway.getInstance().getPortNumber(clusterId));
                CommunicationThread communicationThread = new CommunicationThread(socket,this, clusterId);
                communicationThread.start();
                //Gateway.getInstance().addToSocketMap(socket, clusterId);
                System.out.println(clusterId + Gateway.getInstance().getIp(clusterId) + Gateway.getInstance().getPortNumber(clusterId));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
