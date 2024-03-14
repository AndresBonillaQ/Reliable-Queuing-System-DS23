package network.dnscommunication;

import network.server.model.GateWay;

import java.io.*;
import java.net.Socket;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Questa classe stabilisce una connessione con il DNS, recupera gli indirizzi dei broker e apre con loro la comunicazione
 */
public class HandleDNS {

    static final int portDNS = 8090;
    static final String ipDNS = "localhost";
    private Socket socket;
    Queue<String> messageToSend = new PriorityQueue<>();
    Queue<String> answerReceived = new PriorityQueue<>();
    private static HandleDNS instance = null;


    public static HandleDNS getInstance() throws IOException {
        if (instance == null) {
            instance = new HandleDNS();
        }
        return instance;
    }

//apre la connessione col DNS
    public void openDNSConnection() throws IOException {
        socket = new Socket(ipDNS, portDNS);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        //thread for reading the answers of the DNS
        Thread readerThread = new Thread(() -> {
            try {
                while (true) {
                    String message = in.readLine();
                    //synchronized (answerReceived) {
                        answerReceived.add(message);
                       //  System.out.println("Messaggio dal server: " + message);
                    Thread.sleep(1000);
                    //}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        readerThread.start();

        // Thread for sending requests to the DNS
        Thread writerThread = new Thread(() -> {
            try {
                while (true) {
                   // synchronized (messageToSend) {
                    if (messageToSend != null) {
                        String request = messageToSend.poll();
                        out.println(request);
                    }
                        Thread.sleep(1000);  // Puoi inserire una pausa se necessario
                    }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        writerThread.start();

}

    public void initialize() throws IOException {
        for (String clusterId : GateWay.getInstance().getClusterID()) {
            GateWay.getInstance().setIp(clusterId, requestIP(clusterId));
        }
    }
//messaggio da inviare al DNS
    public String requestIP(String clusterID) {
        messageToSend.add(clusterID);
        if (answerReceived.poll() != null)
            return answerReceived.poll();
        else return null;
    }


}
