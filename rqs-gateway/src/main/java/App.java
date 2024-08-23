import network.brokerCommunication.client.ConnectionManager;
import network.clientCommunication.network.ServerForClient;
import network.clientCommunication.model.Gateway;
import network.clientCommunication.model.initialization.ReadConfigFile;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {

        //apro il server per i clients
        ServerForClient gateWayServer = new ServerForClient(8081);
        gateWayServer.start();

        //inizializzo il gateway con gli ip e porte dei broker
        ReadConfigFile.initialization();

        //apro le connessioni con i broker

        //For testing
        Gateway.getInstance().setPortNumber(8090, "cluster1");
        Gateway.getInstance().addToQueueToClusterMap("queue1", "cluster1");


    }
}