package network.server.model;

import com.google.gson.Gson;
import messages.MessageRequest;
import messages.MessageResponse;
import messages.ResponseBuilder;
import messages.requests.AppendValueRequest;
import messages.requests.CreateQueueRequest;
import messages.requests.ReadValueRequest;
import network.DNScommunication.HandleDNS;
import network.client.ClientThread;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Assunzioni: Il gateway, quando viene instaziato ha la lista dei clusterId, una mappa che associa queueID e clusterID e le porte
 * per comunicare con i broker.
 */
public class GateWay {
    private static GateWay instance = null;
    private int queueId;
    private int numberOfBrokers;
    private HashMap<String, String> clusterIdToAddressMap = new HashMap<String, String>(); //<clusterId, ip>
    private HashMap<String, Integer> clusterToPortMap = new HashMap<String, Integer>();
    private HashMap<String, Socket> socketHashMap = new HashMap<>();
    private ArrayList<String> clusterID = new ArrayList<>(); //Da inizializzare
    private HashMap<String, String> queueToClusterIdMap = new HashMap<String, String>(); //<queueId, clusterId>
    private String currentClusterId = null;

    private String address;
    private int portNumber;
    private Socket socket;

        public static GateWay getInstance() throws IOException {
        if (instance == null) {
            instance = new GateWay();
        }
        return instance;
    }
    public Integer getQueueId() {
        return queueId;
    }

    private String deserializeMessage(StringBuilder jsonData) {
        MessageRequest message = new Gson().fromJson(jsonData.toString(), MessageRequest.class);
        String messageId = message.getId();
        ResponseBuilder responseBuilder = new ResponseBuilder();

        switch (messageId) {
            case "appendValueReq" -> {
                Gson gson = new Gson();
                String queue = gson.fromJson(jsonData.toString(), AppendValueRequest.class).getQueueId();
                currentClusterId = queueToClusterIdMap.get(queue);
                socket = socketHashMap.get(queueToClusterIdMap.get(queue));
                return responseBuilder.createAppendValueResponse(gson.fromJson(jsonData.toString(), AppendValueRequest.class));
            }
            case "createQueueReq" -> {
                String string = responseBuilder.createNewQueueResponse(new Gson().fromJson(jsonData.toString(), CreateQueueRequest.class), String.valueOf(queueId));
                currentClusterId = queueToClusterIdMap.get(String.valueOf(queueId));
                socket = socketHashMap.get(queueToClusterIdMap.get(String.valueOf(queueId)));
                queueId = (queueId + 1) % numberOfBrokers ;
                return string;
            }
            case "readValueReq" -> {
                Gson gson = new Gson();
                String queue = gson.fromJson(jsonData.toString(), ReadValueRequest.class).getQueueId();
                currentClusterId = queueToClusterIdMap.get(queue);
                socket = socketHashMap.get(queueToClusterIdMap.get(queue));
                return responseBuilder.createReadValueResponse(gson.fromJson(jsonData.toString(), ReadValueRequest.class));
            }
        }
        return null;


    }
    private String createMessage(StringBuilder jsonData) {

        MessageResponse message = new MessageResponse();
        message.setId(new Gson().fromJson(jsonData.toString(), MessageRequest.class).getId());
        message.setContent(deserializeMessage(jsonData));
        return new Gson().toJson(message, MessageResponse.class);

    }
    public void execute(StringBuilder jsonData)  { //viene creato il messaggio e dato al clientThread

        ClientThread clientThread;
        try {
            if (!socket.isClosed()) {
                clientThread = new ClientThread(socket, createMessage(jsonData));
                clientThread.run();
            } else {
                HandleDNS.getInstance().request(currentClusterId);
                execute(jsonData);
            }
        } catch (IOException e) {

        }
    }

    public void setNumberOfBrokers(int numberOfBrokers) {
        this.numberOfBrokers = numberOfBrokers;
    }

    public void fillHashMaps(String clusterId, String ipAddress, int portNumber, Socket socket) {
        clusterIdToAddressMap.put(clusterId, ipAddress);
        clusterToPortMap.put(clusterId, portNumber);
        socketHashMap.put(clusterId, socket);
    }

    public void setIp(String clusterId, String ipAddress) {
            this.clusterIdToAddressMap.put(clusterId, ipAddress);
    }
    public String getIp( String clusterID) {
            if ( clusterIdToAddressMap.get(clusterID) != null)
                return clusterIdToAddressMap.get(clusterID);
            else return null;
    }
    public ArrayList<String> getClusterID() {
        return clusterID;
    }

    public void setClusterID(ArrayList<String> clusterID) {
        this.clusterID = clusterID;
    }
    public int getPortNumber(String clusterID) {
            return clusterToPortMap.get(clusterID);
    }
    public void addClusterID(String clusterID) {
            this.clusterID.add(clusterID);
    }
}
