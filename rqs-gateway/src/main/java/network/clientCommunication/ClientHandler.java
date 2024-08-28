package network.clientCommunication;

import java.io.*;
import java.net.Socket;

import messages.MessageRequest;
import messages.MessageResponse;
import messages.requests.SetUpRequest;
import messages.id.ResponseIdEnum;
import messages.responses.SetUpResponse;
import messages.responses.StatusEnum;
import model.Gateway;
import utils.GsonInstance;


/**
 * per ogni client che richiede la connessione col server viene spawnato un thread che lo "gestisce"
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private String clientID;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
    }

    @Override
    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter outputStream = new PrintWriter(clientSocket.getOutputStream());

            // the first message a client send is the "set_up" message
            setUpClient(reader, outputStream);

            while (!clientSocket.isClosed()) {

                //gateway reads the request from the client
                String inputLine;
                while ( ( inputLine =  reader.readLine() ) != null) {
                    MessageRequest messageRequest = GsonInstance.getInstance().getGson().fromJson(inputLine, MessageRequest.class);
                    System.out.println("Request from client: " + messageRequest.toString());
                    synchronized (Gateway.getInstance()) {
                        clientID = Gateway.getInstance().processRequest(messageRequest);
                    }
                }
                //ogni volta che la risposta per un determinato client diventa disponibile viene inoltrata al suddetto client
                if (Gateway.getInstance().fetchResponse(clientID) != null) {
                    outputStream.println(GsonInstance.getInstance().getGson().toJson(Gateway.getInstance().fetchResponse(clientID) ));
                    outputStream.flush();
                }

            }

        } catch (IOException e) {
            System.out.println("ERROR ON CLIENT CONNECTION: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void setUpClient(BufferedReader in, PrintWriter out) throws IOException {

        String readLine = in.readLine();
        System.out.println("Setup client request: " + readLine);

        MessageRequest messageRequest = GsonInstance.getInstance().getGson().fromJson(readLine, MessageRequest.class);
        SetUpRequest setUpRequest = GsonInstance.getInstance().getGson().fromJson(messageRequest.getContent(), SetUpRequest.class);

        MessageResponse responseMessage;

        if(Gateway.getInstance().registerClientOnResponseMap(setUpRequest.getClientId())){

            System.out.println("Registering client" + setUpRequest.getClientId());
            SetUpResponse setUpResponse = new SetUpResponse(StatusEnum.OK, "", setUpRequest.getClientId());
            responseMessage = new MessageResponse(
                    ResponseIdEnum.SET_UP_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(setUpResponse),
                    setUpRequest.getClientId()
            );

        } else {

            SetUpResponse setUpResponse = new SetUpResponse(StatusEnum.KO, "ClientId already registered!", setUpRequest.getClientId());
            responseMessage = new MessageResponse(
                    ResponseIdEnum.SET_UP_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(setUpResponse),
                    setUpRequest.getClientId()
            );

        }

        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();
    }
}