import network.brokerCommunication.server.ServerForBroker;
import network.clientCommunication.network.ServerForClient;

import java.io.IOException;

public class GatewayApp {
    public static void main(String[] args) throws IOException {

        //apro il server per i clients
        new Thread(new ServerForClient(6666)).start();

        new Thread(new ServerForBroker(5001)).start();
        //inizializzo il gateway con gli ip e porte dei broker
       // ReadConfigFile.initialization();

        //apro le connessioni con i broker

        //For testing
       // Gateway.getInstance().setPortNumber(8090, "cluster1");
        // Gateway.getInstance().addToQueueToClusterMap("queue1", "cluster1");


    }
}