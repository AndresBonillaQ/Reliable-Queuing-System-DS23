import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {

    private static final Logger log = Logger.getLogger(Main.class.getName());
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {

        try(
                ServerSocket serverSocket = new ServerSocket(8081);
        ){

            Socket socket;

            while(true){
                try{
                    socket = serverSocket.accept();
                    log.severe("New client is connected!");
                }catch (IOException e){
                    log.severe("IO Exception during client connection");
                    return;
                }

                run(socket);
            }

        } catch (IOException ex) {
            log.severe("Error! IOException");
            log.severe(ex.getMessage());
        }
    }

    private static void run(Socket clientSocket){
        try(
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ){

            String request;
            clientSocket.setSoTimeout(5000);

            while(true){

                try{
                    //Receive request from socket
                    request = in.readLine();
                }catch (IOException e){
                    log.severe("Connection error with client, maybe disconnected!");
                    return;
                }

                log.info("Received:"  + request);

                CreateQueueResponse response = new CreateQueueResponse();
                response.setStatus(StatusEnum.OK);
                response.setDesStatus("CIAO");

                ResponseMessage responseMessage = new ResponseMessage();
                responseMessage.setId(ResponseIdEnum.CREATE_QUEUE_RESPONSE);
                responseMessage.setContent(GsonInstance.getInstance().getGson().toJson(response));

                //Send response to socket
                writer.println(GsonInstance.getInstance().getGson().toJson(responseMessage));
                writer.flush();
            }

        } catch (IOException ex) {
            log.severe("Error! IOException");
            log.severe(ex.getMessage());
        }
    }
}

