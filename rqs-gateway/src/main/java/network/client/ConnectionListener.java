package network.client;

public interface ConnectionListener {

    void onConnectionLost(String clusterID);
}
