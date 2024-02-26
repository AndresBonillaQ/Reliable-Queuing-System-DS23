package network.DNScommunication;

import com.google.gson.Gson;
import messages.FromDNS.MessageFromDNS;
import messages.MessageRequest;
import messages.messagesToDNS.MessageToDNS;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

/**
 * Questa classe stabilisce una connessione con il DNS, recupera gli indirizzi dei broker e apre con loro la comunicazione
 */
public class ConnectionToDNS {

    static final int portDNS = 8090;
    static final String ipDNS = "localhost";
    private  BufferedReader in;
    private Socket socket;

    private Gson gson;

    public HashMap<String, Integer> start() throws IOException, ClassNotFoundException {
        Socket socket = new Socket(ipDNS, portDNS); //stabilita connessione con DNS
        this.socket = socket;
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(new Gson().toJson(( new MessageToDNS("Init")), MessageToDNS.class).getBytes());
        outputStream.flush();
        ObjectInputStream inputStream = null;

        inputStream = new ObjectInputStream(socket.getInputStream());

        HashMap<String, Integer> receivedData = (HashMap<String, Integer>) inputStream.readObject();

        return receivedData;
    }

    public void getIp() {

    }

}
