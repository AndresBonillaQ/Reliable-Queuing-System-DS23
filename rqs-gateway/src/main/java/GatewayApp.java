import model.Gateway;
import network.brokerCommunication.server.ServerForBroker;
import network.clientCommunication.ServerForClient;

import java.io.IOException;

public class GatewayApp {
    public static void main(String[] args) throws IOException {

        //apro il server per i clients
        new Thread(new ServerForClient(6666)).start();

        new Thread(new ServerForBroker(5001)).start();


    }
}