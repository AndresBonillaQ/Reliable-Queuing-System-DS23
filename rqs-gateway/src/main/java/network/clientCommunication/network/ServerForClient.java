package network.clientCommunication.network;

import network.clientCommunication.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Questa classe attende connessioni dai client, per ogni client che si connette crea un thread (clientHandler).
 */

public class ServerForClient {

    private final int portNumber;
    public ServerForClient(int portNumber) {
        this.portNumber = portNumber;
    }

    public void start() throws IOException {

        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("Gateway server is awaiting connections...");
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Accepted");

                //per ogni client con cui viene aperta la connessione viene spawnato un clientHandler per quel client
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
