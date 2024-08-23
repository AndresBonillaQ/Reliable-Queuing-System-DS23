package network.clientCommunication.model;

import com.google.gson.Gson;
import messages.MessageRequest;
import messages.MessageResponse;
import messages.requests.AppendValueRequest;
import messages.requests.CreateQueueRequest;
import messages.requests.ReadValueRequest;
import messages.requests.RequestIdEnum;
import messages.responses.*;
import network.brokerCommunication.client.ConnectionManager;
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
    private static ConnectionManager connectionManager;

    private int queueSequenceNumber = -1 ;
    public static Gateway getInstance() throws IOException {
        if (instance == null) {
            instance = new Gateway();
        }

        if (connectionManager == null )
            connectionManager =  new ConnectionManager();

        return instance;
    }

    private String generateNewQueueID() {
        queueSequenceNumber++;
        return String.valueOf(queueSequenceNumber);
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

        if (clusterConnected.get(clusterID) == null)
            return false;
        return (clusterConnected.get(clusterID) == 1);


    }
    public MessageResponse fetchResponse(String clientID) {
        if (responseMessageMap.getMessageQueue(clientID) != null)
            return responseMessageMap.getMessageQueue(clientID).poll();
        else return null;
    }


    public String processRequest(MessageRequest messageRequest) throws IOException {
        String messageId = messageRequest.getId().getValue();

        switch (messageId) {

            case "appendValueReq" -> {
                AppendValueRequest request =  (new Gson() ).fromJson( (new Gson()).toJson(messageRequest.getContent()) , AppendValueRequest.class);
                //metto il prossimo messaggio da inviare al cluster
                requestsMap.putOnRequestQueue(queueToClusterIdMap.get (request.getQueueId() ), messageRequest);
            }

            // La richiesta di creazione di una queue viene inviata al broker scelto secondo la logica "round robin"
            case "createQueueReq" -> {
                CreateQueueRequest request =  (new Gson() ).fromJson( (new Gson()).toJson(messageRequest.getContent()) , CreateQueueRequest.class);
                request.setQueueID(generateNewQueueID());
                messageRequest.setContent((new Gson() ).toJson(request)) ;
                if (!nextCluster.containsValue(0)) {
                    for (String clusterID : clustersID) {
                        nextCluster.put(clusterID, 0);
                    }
                }
                for (String clusterID : clustersID) {
                    if (nextCluster.get(clusterID) == 0) {
                        requestsMap.putOnRequestQueue(clusterID, messageRequest);
                        queueToClusterIdMap.put(String.valueOf(queueSequenceNumber), clusterID);
                        nextCluster.replace(clusterID, 1);
                        break;
                    }
                }


            }
            case "readValueReq" -> {
                ReadValueRequest request =  (new Gson() ).fromJson( (new Gson()).toJson(messageRequest.getContent()) , ReadValueRequest.class);
                //metto il prossimo messaggio da inviare al cluster
                requestsMap.putOnRequestQueue(queueToClusterIdMap.get (request.getQueueId() ), messageRequest);
            }
        }
        return null;

    }

    public String processResponse(MessageResponse messageResponse) throws IOException {
        String messageId = messageResponse.getId();
        String clientId = messageResponse.getClientID();

        if (ResponseIdEnum.RECONNECTION_MESSAGE.equals(messageId) ) {

                ReconnectionMessage response =  (new Gson() ).fromJson( (new Gson()).toJson(messageResponse.getContent()) , ReconnectionMessage.class);
                String clusterID = response.getClusterId();
                String ipAddress = response.getIpAddress();
                Integer portNumber = response.getPortNumber();

                clustersID.add(clusterID);
                clusterIdToAddressMap.put(clusterID, ipAddress);
                clusterToPortMap.put(clusterID, portNumber);
                clusterConnected.put(clusterID, 1);
                requestsMap.addClusterID(clusterID);

                if (nextCluster.get(clusterID) == null )
                    nextCluster.put(clusterID, 0);

                connectionManager.startConnection(clusterID);
            }
        else {
            putOnResponseMap(clientId, messageResponse);
        }
        return clientId;
    }

public void putOnResponseMap(String clientID, MessageResponse messageResponse) {
        this.responseMessageMap.putOnResponseQueue(clientID, messageResponse);
}



}
