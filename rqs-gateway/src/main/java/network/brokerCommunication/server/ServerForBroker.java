package network.brokerCommunication.server;

import network.clientCommunication.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerForBroker {

    private final int portNumber;
    public ServerForBroker(int portNumber) {
        this.portNumber = portNumber;
    }
    public void start() throws IOException {

        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("Gateway server is awaiting connections...");

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Accepted");
                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                System.out.println("socket close..." + e.getMessage());
                break;
            }
        }
        System.out.println("Closing Gateway server");
        serverSocket.close();
    }
}



