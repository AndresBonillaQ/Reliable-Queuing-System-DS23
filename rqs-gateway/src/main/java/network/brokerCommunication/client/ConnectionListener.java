package network.brokerCommunication.client;

import java.io.IOException;

public interface ConnectionListener {

    void onConnectionLost(Integer clusterID) throws IOException;
}
