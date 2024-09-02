package network.clientCommunication;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
            ExecutorService executorService = Executors.newFixedThreadPool(1);

            // the first message a client send is the "set_up" message
            setUpClient(reader, outputStream);

            while (!clientSocket.isClosed()) {

                //gateway reads the request from the client
                String inputLine;
                inputLine =  reader.readLine();
                MessageRequest messageRequest = GsonInstance.getInstance().getGson().fromJson(inputLine, MessageRequest.class);

                System.out.println("Request from client: " + messageRequest.toString());
                synchronized (Gateway.getInstance()) {
                    clientID = Gateway.getInstance().processRequest(outputStream, messageRequest);
                }

                fetchBrokerResponse(executorService, outputStream, clientSocket);

                //ogni volta che la risposta per un determinato client diventa disponibile viene inoltrata al suddetto clien
            }

            System.out.println("Socket chiuso, task terminato.");
            executorService.shutdown();

        } catch (IOException e) {
            System.out.println("ERROR ON CLIENT CONNECTION: " + e.getMessage());
            Gateway.getInstance().removeClientId(clientID);
            throw new RuntimeException(e);
        }
    }

    private void setUpClient(BufferedReader in, PrintWriter out) throws IOException {

        String readLine = in.readLine();
        System.out.println("Setup client request: " + readLine);

        MessageRequest messageRequest = GsonInstance.getInstance().getGson().fromJson(readLine, MessageRequest.class);
        SetUpRequest setUpRequest = GsonInstance.getInstance().getGson().fromJson(messageRequest.getContent(), SetUpRequest.class);

        MessageResponse responseMessage;

        if(!Gateway.getInstance().isClientPresent(messageRequest.getClientId())){

            synchronized (Gateway.getInstance()) {
                clientID = String.valueOf(Gateway.getInstance().generateNewClientID());
            }

            System.out.println("Registering client" + clientID);
            SetUpResponse setUpResponse = new SetUpResponse(StatusEnum.OK, "", clientID);
            responseMessage = new MessageResponse(
                    ResponseIdEnum.SET_UP_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(setUpResponse),
                    clientID
            );
            Gateway.getInstance().registerClientOnResponseMap(clientID);

        } else {

            SetUpResponse setUpResponse = new SetUpResponse(StatusEnum.KO, "ClientId already registered!", messageRequest.getClientId());
            responseMessage = new MessageResponse(
                    ResponseIdEnum.SET_UP_RESPONSE,
                    GsonInstance.getInstance().getGson().toJson(setUpResponse),
                    messageRequest.getClientId()
            );

        }

        out.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
        out.flush();
    }

    private void fetchBrokerResponse(ExecutorService executorService,PrintWriter outputStream, Socket clientSocket ) {
        executorService.submit(() -> {
            while (!clientSocket.isClosed()) {
                // synchronized (Gateway.getInstance()) {
                while (Gateway.getInstance().newMessageOnQueue(clientID)) {
                    outputStream.println(GsonInstance.getInstance().getGson().toJson(Gateway.getInstance().fetchResponse(clientID)));
                    outputStream.flush();
                    System.out.println("Risposta inoltrata al client");
                }
                try {
                    // Attende un breve periodo per ridurre il consumo di CPU
                    Thread.sleep(500);  // attende 500 ms
                } catch (InterruptedException e) {
                    System.out.println("Thread interrotto.");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }
}