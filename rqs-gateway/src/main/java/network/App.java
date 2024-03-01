package network;

import network.DNScommunication.HandleDNS;
import network.client.ClientConnection;
import network.server.GateWayServer;
import network.server.model.GateWay;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {



        //For testing
        GateWay.getInstance().addClusterID("cluster1");
        GateWay.getInstance().setPortNumber(8090, "cluster1");
        GateWay.getInstance().addToQueueToClusterMap("queue1", "cluster1");

        //ottengo gli ip dei broker
        HandleDNS.getInstance().openDNSConnection();
        HandleDNS.getInstance().initialize();

        //apro le connessioni con i broker
        ClientConnection clientConnection = new ClientConnection();
        clientConnection.openConnection();

        //apro il server per i clients
        GateWayServer gateWayServer = new GateWayServer(8081);
        gateWayServer.start();
    }
}