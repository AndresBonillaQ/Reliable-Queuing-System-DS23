import model.Gateway;
import network.brokerCommunication.server.ServerForBroker;
import network.clientCommunication.ServerForClient;

public class GatewayApp {

    public static void main(String[] args) {
        start();
    }

    public static void start(){
        Gateway.getInstance().setMaxNumberOfClusters(2);
        new Thread(new ServerForClient(6666)).start();
        new Thread(new ServerForBroker(5001)).start();
    }
}