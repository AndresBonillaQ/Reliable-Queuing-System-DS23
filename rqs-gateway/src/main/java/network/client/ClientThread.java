package network.client;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class ClientThread implements Runnable{

    private String clusterId;
    private String jsonData;
    private String ipAddress;
    private int portNumber;
    private Socket socket;




    public ClientThread(Socket socket, String jsonData) throws IOException {
        this.jsonData = jsonData;
        this.socket = socket;
    }

    @Override
    public void run() {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(jsonData.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
}
