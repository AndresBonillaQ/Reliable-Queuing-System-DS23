package network;

import network.client.ClientConnection;
import network.server.GateWayServer;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {

        ClientConnection clientConnection = new ClientConnection();
        clientConnection.openConnection();

        GateWayServer gateWayServer = new GateWayServer(8081);
        gateWayServer.start();
    }
}