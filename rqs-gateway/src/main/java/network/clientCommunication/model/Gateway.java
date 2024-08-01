package network.clientCommunication.model;

import com.google.gson.Gson;
import messages.MessageRequest;
import messages.MessageResponse;
import messages.requests.AppendValueRequest;
import messages.requests.CreateQueueRequest;
import messages.requests.ReadValueRequest;
import messages.responses.AppendValueResponse;
import messages.responses.CreateQueueResponse;
import messages.responses.ReconnectionMessage;
import network.clientCommunication.model.utils.RequestMessageMap;
import network.clientCommunication.model.utils.ResponseMessageMap;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Assunzioni: Il gateway, quando viene instanziato ha la lista dei clusterId, una mappa che associa queueID e clusterID e le porte
 * per comunicare con i broker.
 */
public class Gateway {
    private static Gateway instance = null;
    private HashMap<String, String> clusterIdToAddressMap = new HashMap<String, String>(); //<clusterId, ip>
    private HashMap<String, Integer> clusterToPortMap = new HashMap<String, Integer>();
    private ArrayList<String> clustersID = new ArrayList<>(); //Da inizializzare
    private HashMap<String, String> queueToClusterIdMap = new HashMap<String, String>(); //<queueId, clusterId>
    private ResponseMessageMap responseMessageMap = new ResponseMessageMap();
    private RequestMessageMap requestsMap = new RequestMessageMap();//<clusterId, QueueOfRequests>
    private HashMap<String, Integer> nextCluster = new HashMap<>();
    private HashMap<String, Integer> clusterConnected = new HashMap<>();
    public static Gateway getInstance() throws IOException {
        if (instance == null) {
            instance = new Gateway();
        }
        return instance;
    }
    public void fillHashMaps(String clusterId, String ipAddress, int portNumber) {
        clusterIdToAddressMap.put(clusterId, ipAddress);
        clusterToPortMap.put(clusterId, portNumber);
        nextCluster.put(clusterId, 0);
        clusterConnected.put(clusterId, 1);
    }
    public void setIp(String clusterId, String ipAddress) {
            this.clusterIdToAddressMap.put(clusterId, ipAddress);
    }
    public String getIp(String clusterID) throws IOException {
            if ( clusterIdToAddressMap.get(clusterID) != null)
                return clusterIdToAddressMap.get(clusterID);
            else return null;
    }
    public ArrayList<String> getClustersID() {
        return clustersID;
    }
    public int getPortNumber(String clusterID) {
            return clusterToPortMap.get(clusterID);
    }
    public void setPortNumber(int portNumber, String clusterID) {
        clusterToPortMap.put(clusterID, portNumber);
    }

    public void addToQueueToClusterMap(String queueID, String clusterID) {
        queueToClusterIdMap.put(clusterID, queueID);
    }
    public MessageRequest pollRequest(String clusterID) {
        return requestsMap.getMessageQueue(clusterID).poll();
    }
    public String getClusterID(String queueID) {
            return queueToClusterIdMap.get(queueID);
    }

    public void addToResponseQueue(String jsonData) {
        Gson gson = new Gson();
        String clientID = gson.fromJson(jsonData, MessageResponse.class).getClientID();
        MessageResponse messageResponse = gson.fromJson(jsonData, MessageResponse.class);
        responseMessageMap.putOnResponseQueue(clientID, messageResponse);

    }

    public void setClusterAsDisconnected(String clusterID) {
        clusterConnected.replace(clusterID, 0);
    }
    public boolean isUpdated(String clusterID) {
        return (clusterConnected.get(clusterID) == 1);
    }
    public MessageResponse fetchResponse(String clientID) {
        if (responseMessageMap.getMessageQueue(clientID) != null)
            return responseMessageMap.getMessageQueue(clientID).poll();
        else return null;
    }

    public String processRequest(MessageRequest jsonData) throws IOException {
        String messageId = jsonData.getId().getValue();
        switch (messageId) {

            case "appendValueReq"-> {
                AppendValueRequest request =  (new Gson() ).fromJson( (new Gson()).toJson(jsonData.getContent()) , AppendValueRequest.class);
                //metto il prossimo messaggio da inviare al cluster
                requestsMap.putOnRequestQueue(queueToClusterIdMap.get (request.getQueueId() ), jsonData);
            }

            // La richiesta di creazione di una queue viene inviata al broker scelto secondo la logica "round robin"
            case "createQueueReq" -> {
                if (!nextCluster.containsValue(0))
                    for (String clusterID : clustersID) {
                        nextCluster.put(clusterID, 0);
                    }
                for (String clusterID : clustersID) {
                    if (nextCluster.get(clusterID) == 0) {
                        CreateQueueRequest request =  (new Gson() ).fromJson( (new Gson()).toJson(jsonData.getContent()) , CreateQueueRequest.class);
                        requestsMap.putOnRequestQueue(clusterID, jsonData);
                        nextCluster.replace(clusterID, 1);
                        break;
                    }
                }
            }
            case "readValueReq" -> {
                ReadValueRequest request =  (new Gson() ).fromJson( (new Gson()).toJson(jsonData.getContent()) , ReadValueRequest.class);
                //metto il prossimo messaggio da inviare al cluster
                requestsMap.putOnRequestQueue(queueToClusterIdMap.get (request.getQueueId() ), jsonData);

            }
        }
        return null;

    }

    public String processResponse(MessageResponse jsonData) throws IOException {
        String messageId = jsonData.getId();
        String clientId = jsonData.getClientID();
        switch (messageId) {

            case "appendValueResp"-> {
                AppendValueResponse response =  (new Gson() ).fromJson( (new Gson()).toJson(jsonData.getContent()) , AppendValueResponse.class);
                //metto il prossimo messaggio da inviare al cluster
               responseMessageMap.putOnResponseQueue(clientId, jsonData);
            }
            case "createQueueResp" -> {
                CreateQueueResponse response =  (new Gson() ).fromJson( (new Gson()).toJson(jsonData.getContent()) , CreateQueueResponse.class);
                responseMessageMap.putOnResponseQueue(clientId, jsonData);
            }
            case "readValueResp" -> {
                ReadValueRequest response =  (new Gson() ).fromJson( (new Gson()).toJson(jsonData.getContent()) , ReadValueRequest.class);
                responseMessageMap.putOnResponseQueue(clientId, jsonData);
            }
            case "reconnection" -> {

                ReconnectionMessage response =  (new Gson() ).fromJson( (new Gson()).toJson(jsonData.getContent()) , ReconnectionMessage.class);
                String clusterID = response.getClusterId();
                String ipAddress = response.getIpAddress();
                Integer portNumber = response.getPortNumber();

                queueToClusterIdMap.replace(queueToClusterIdMap.get(clusterID), clusterID);
                clusterIdToAddressMap.replace(clusterID, ipAddress);
                clusterToPortMap.replace(clusterID, portNumber);
                clusterConnected.replace(clusterID, 1);
            }
        }
        return null;
    }





}
