import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
public class ServerDNS {

    private Socket serverSocket;

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8090);
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
        private static void handleClient(Socket clientSocket) {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String clientMessage;
                while ((clientMessage = in.readLine()) != null) {
                    String response = processDnsRequest(clientMessage);

                    out.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    /**
     *riceve in input il clusterId e ritorna l'indirizzo ip del leader di quel cluster
     */

    private static String processDnsRequest(String request) {
            return DNS.getInstance().getAddress(request);
        }
}
