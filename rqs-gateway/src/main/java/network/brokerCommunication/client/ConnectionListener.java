package network.brokerCommunication.client;

import java.io.IOException;

public interface ConnectionListener {

    void onConnectionLost(String clusterID) throws IOException;
}
