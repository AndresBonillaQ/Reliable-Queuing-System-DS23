package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public abstract class Server {

    private Socket serverSocket;
    private final int portNumber;

    public Server(int portNumber) {
        this.portNumber = portNumber;
    }
        public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("DNS server is awaiting connections...");

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Accepted");
                while (!socket.isClosed()) {
                    handleClient(socket);
                }
            } catch (IOException e) {
                System.out.println("socket close..." + e.getMessage());
                break;
            }
        }
        System.out.println("Closing Gateway server");
        serverSocket.close();
    }
    public void handleClient(Socket socket){}
}
