import java.io.*;
import java.net.Socket;
public class ClientHandler extends Server{
    private Socket serverSocket;
    private int portNumber;

    public ClientHandler(int portNumber) {
        super(portNumber);
    }

    public void handleClient(Socket clientSocket) {
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
            return Dns.getInstance().getAddress(request);
        }

}

