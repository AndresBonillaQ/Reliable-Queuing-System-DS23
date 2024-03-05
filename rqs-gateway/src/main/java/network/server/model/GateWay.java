package network.server.model;

import com.google.gson.Gson;
import messages.MessageRequest;
import messages.MessageResponse;
import messages.requests.AppendValueRequest;
import messages.requests.CreateQueueRequest;
import messages.requests.CreateQueueRequestToSend;
import messages.requests.ReadValueRequest;
import network.dnscommunication.HandleDNS;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

/**
 * Assunzioni: Il gateway, quando viene instaziato ha la lista dei clusterId, una mappa che associa queueID e clusterID e le porte
 * per comunicare con i broker.
 */
public class GateWay {
    private static GateWay instance = null;
    private int queueId;
    private HashMap<String, String> clusterIdToAddressMap = new HashMap<String, String>(); //<clusterId, ip>
    private HashMap<String, Integer> clusterToPortMap = new HashMap<String, Integer>();
    private HashMap<String, Socket> socketHashMap = new HashMap<>();
    private HashMap<Integer, String> clusterNumberToClusterID = new HashMap<>(); //< clusterID, numberOfQueues>
    private ArrayList<String> clusterID = new ArrayList<>(); //Da inizializzare
    private HashMap<String, String> queueToClusterIdMap = new HashMap<String, String>(); //<queueId, clusterId>
    private int currentCluster = 0;
    private RequestMessageMap requestMessageMap = new RequestMessageMap();
    private ResponseMessageMap responseMessageMap = new ResponseMessageMap();
    public GateWay() throws IOException {
    }

    public static GateWay getInstance() throws IOException {
        if (instance == null) {
            instance = new GateWay();
        }
        return instance;
    }
    public void fillHashMaps(String clusterId, String ipAddress, int portNumber, Socket socket) {
        clusterIdToAddressMap.put(clusterId, ipAddress);
        clusterToPortMap.put(clusterId, portNumber);
        socketHashMap.put(clusterId, socket);
    }
    public void setIp(String clusterId, String ipAddress) {
            this.clusterIdToAddressMap.put(clusterId, ipAddress);
    }
    public String getIp(String clusterID) throws IOException {
            if ( clusterIdToAddressMap.get(clusterID) != null)
                return clusterIdToAddressMap.get(clusterID);
            else return HandleDNS.getInstance().requestForInit(clusterID);
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
    public void setPortNumber(int portNumber, String clusterID) {
        clusterToPortMap.put(clusterID, portNumber);
    }
    public void addClusterID(String clusterID) {
            this.clusterID.add(clusterID);
    }
    public void addToSocketMap(Socket socket, String clusterID) {
        socketHashMap.put(clusterID, socket);
    }
    public void addToQueueToClusterMap(String queueID, String clusterID) {
        queueToClusterIdMap.put(clusterID, queueID);
    }
    public String getClusterID(String queueID) {
            return queueToClusterIdMap.get(queueID);
    }
    public Socket getSocket(String queueID) {
            return socketHashMap.get(getClusterID(queueID));
    }
    public void addToResponseQueue(StringBuilder jsonData) {
        Gson gson = new Gson();
        String clientID = gson.fromJson(jsonData.toString(), MessageResponse.class).getClientID();
        responseMessageMap.putOnResponseQueue(clientID, jsonData.toString());

    }
    public BlockingQueue<String> fetchRequest(String clusterId) {
            return requestMessageMap.getMessageQueue(clusterId);
    }
    public BlockingQueue<String> fetchResponse(String clientID) {
        return responseMessageMap.getMessageQueue(clientID);
    }
    public void addToClusterNumberToClusterID (int clusterNumber, String clusterID) {
        clusterNumberToClusterID.put(clusterNumber, clusterID);
    }
    public ResponseMessageMap getResponseMessageMap() {
        return this.responseMessageMap;
    }
    public RequestMessageMap getRequestMessageMap() {
        return this.requestMessageMap;
    }
    public String processRequest(StringBuilder jsonData) {
        MessageRequest message = new Gson().fromJson(jsonData.toString(), MessageRequest.class);
        String messageId = message.getId();
        switch (messageId) {
            case "appendValueReq"-> {
                Gson gson = new Gson();
                String clientId = gson.fromJson(jsonData.toString(), AppendValueRequest.class).getClientId();
                responseMessageMap.addClientID(clientId);
                String queueID = gson.fromJson(jsonData.toString(), AppendValueRequest.class).getQueueId();
                requestMessageMap.putOnRequestQueue(queueToClusterIdMap.get(queueID), message.toString());
                return clientId;
            }
            case "createQueueReq" -> {
                Gson gson = new Gson();
                CreateQueueRequest createQueueRequest = gson.fromJson(jsonData.toString(), CreateQueueRequest.class);
                String clientId = createQueueRequest.getClientId();
                responseMessageMap.addClientID(clientId);
                CreateQueueRequestToSend createQueueRequestToSend = new CreateQueueRequestToSend();
                createQueueRequestToSend.setQueueId(String.valueOf(queueId));
                createQueueRequestToSend.setClientId(createQueueRequest.getClientId());
                message.setContent(createQueueRequestToSend.toString());

                queueId++;

                if (clusterNumberToClusterID.containsKey(currentCluster)) {
                    requestMessageMap.putOnRequestQueue(clusterNumberToClusterID.get(currentCluster), message.toString());
                }
                else {
                    currentCluster = 0;
                    requestMessageMap.putOnRequestQueue(clusterNumberToClusterID.get(currentCluster), message.toString());
                }
            currentCluster++;

                return clientId;
            }
            case "readValueReq" -> {
                Gson gson = new Gson();
                String clientId = gson.fromJson(jsonData.toString(), ReadValueRequest.class).getClientId();
                responseMessageMap.addClientID(clientId);
                String queueID = gson.fromJson(jsonData.toString(), ReadValueRequest.class).getQueueId();
                requestMessageMap.putOnRequestQueue(queueToClusterIdMap.get(queueID), message.toString());
                return clientId;
            }
        }
        return null;

    }


}
