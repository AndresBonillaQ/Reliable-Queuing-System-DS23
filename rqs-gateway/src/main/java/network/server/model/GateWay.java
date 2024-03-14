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
            else return HandleDNS.getInstance().requestIP(clusterID);
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
            this.requestMessageMap.addClusterID(clusterID);
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
    public String fetchRequest(String clusterId) {
            return requestMessageMap.getMessageQueue(clusterId).poll();
    }
    public String fetchResponse(String clientID) {
        if (responseMessageMap.getMessageQueue(clientID) != null)
            return responseMessageMap.getMessageQueue(clientID).poll();
        else return null;
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
    public String processRequest(MessageRequest jsonData) throws IOException {
       // MessageRequest message = new Gson().fromJson(jsonData.toString(), MessageRequest.class);
        String messageId = jsonData.getId().getValue();
        switch (messageId) {
            case "appendValueReq"-> {
                Gson gson = new Gson();
                String clientId = gson.fromJson(jsonData.getContent(), AppendValueRequest.class).getClientId();
                responseMessageMap.addClientID(clientId);
                String queueID = gson.fromJson(jsonData.getContent(), AppendValueRequest.class).getQueueId();
                requestMessageMap.putOnRequestQueue(queueToClusterIdMap.get(queueID), jsonData.getContent());
                return clientId;
            }
            case "createQueueReq" -> {
                Gson gson = new Gson();
                CreateQueueRequest createQueueRequest = gson.fromJson(jsonData.getContent(), CreateQueueRequest.class);
                String clientId = createQueueRequest.getClientId();
                responseMessageMap.addClientID(clientId);
                CreateQueueRequestToSend createQueueRequestToSend = new CreateQueueRequestToSend();
                createQueueRequestToSend.setQueueId(String.valueOf(queueId));
                createQueueRequestToSend.setClientId(createQueueRequest.getClientId());
                jsonData.setContent(createQueueRequestToSend.toString());

                //for debug
                Gson gson1 = new Gson();
                String jsonString = gson1.toJson(createQueueRequestToSend);
                System.out.println(jsonString);

                queueId++;

                if (clusterNumberToClusterID.containsKey(currentCluster)) {
                    requestMessageMap.putOnRequestQueue(clusterNumberToClusterID.get(currentCluster), jsonData.toString());
                }
                else {
                    currentCluster = 0;
                    requestMessageMap.putOnRequestQueue(clusterNumberToClusterID.get(currentCluster), jsonData.toString());
                }
            currentCluster++;

                return clientId;
            }
            case "readValueReq" -> {
                Gson gson = new Gson();
                String clientId = gson.fromJson(jsonData.toString(), ReadValueRequest.class).getClientId();
                responseMessageMap.addClientID(clientId);
                String queueID = gson.fromJson(jsonData.toString(), ReadValueRequest.class).getQueueId();
                requestMessageMap.putOnRequestQueue(queueToClusterIdMap.get(queueID), jsonData.toString());
                return clientId;
            }
        }
        return null;

    }


}
