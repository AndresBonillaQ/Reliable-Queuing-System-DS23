package model;

import com.google.gson.Gson;
import it.polimi.ds.message.ResponseMessage;
import messages.MessageRequest;
import messages.MessageResponse;
import messages.connectionSetUp.SetUpConnectionMessage;
import messages.requests.AppendValueRequest;
import messages.requests.CreateQueueRequest;
import messages.requests.ReadValueRequest;
import network.brokerCommunication.client.ConnectionManager;
import model.utils.RequestMessageMap;
import model.utils.ResponseMessageMap;
import utils.GsonInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Assunzioni: Il gateway, quando viene instanziato ha la lista dei clusterId, una mappa che associa queueID e clusterID e le porte
 * per comunicare con i broker.
 */
public class Gateway {
    private static Gateway instance = null;
    private HashMap<Integer, String> clusterIdToAddressMap = new HashMap<Integer, String>(); //<clusterId, ip>
    private HashMap<Integer, Integer> clusterToPortMap = new HashMap<Integer, Integer>();
    private ArrayList<Integer> clustersID = new ArrayList<>(); //Da inizializzare
    private HashMap<Integer, Integer> queueToClusterIdMap = new HashMap<Integer, Integer>(); //<queueId, clusterId>
    private ResponseMessageMap responseMessageMap = new ResponseMessageMap();
    private RequestMessageMap requestsMap = new RequestMessageMap();//<clusterId, QueueOfRequests>
    private HashMap<Integer, Integer> nextCluster = new HashMap<>();
    private HashMap<Integer, Integer> clusterConnected = new HashMap<>();

    private final int maxNumberOfClusters = 1000;

    private static ConnectionManager connectionManager;
    private int queueSequenceNumber = -1 ;

    public static Gateway getInstance() {
        if (instance == null) {
            instance = new Gateway();
        }

        if (connectionManager == null )
            connectionManager =  new ConnectionManager();

        return instance;
    }

    private Integer generateNewQueueID() {
        queueSequenceNumber++;
        return queueSequenceNumber;
    }
    public void setIp(Integer clusterId, String ipAddress) {
            this.clusterIdToAddressMap.put(clusterId, ipAddress);
    }

    public String getIp(Integer clusterID) throws IOException {
            if ( clusterIdToAddressMap.get(clusterID) != null)
                return clusterIdToAddressMap.get(clusterID);
            else return null;
    }

    public ArrayList<Integer> getClustersID() {
        return clustersID;
    }
    public int getPortNumber(Integer clusterID) {
            return clusterToPortMap.get(clusterID);
    }
    public void setPortNumber(int portNumber, Integer clusterID) {
        clusterToPortMap.put(clusterID, portNumber);
    }

    public void addToQueueToClusterMap( Integer queueID, Integer clusterID) {
        queueToClusterIdMap.put(clusterID, queueID);
    }
    public MessageRequest pollRequest(Integer clusterID) {
        return requestsMap.getMessageQueue(clusterID).poll();
    }
    public Integer getClusterID(Integer queueID) {
            return queueToClusterIdMap.get(queueID);
    }

    public void setClusterAsDisconnected(Integer clusterID) {
        clusterConnected.replace(clusterID, 0);
    }
    public boolean isUpdated(Integer clusterID) {
        if (clusterConnected.get(clusterID) == null)
            return false;
        return (clusterConnected.get(clusterID) == 1);

    }
    public MessageResponse fetchResponse(String clientID) {
        if (!responseMessageMap.getMessageQueue(clientID).isEmpty())
            return responseMessageMap.getMessageQueue(clientID).poll();
        else
            return null;
    }


    public String processRequest(MessageRequest messageRequest) throws IOException {

        switch (messageRequest.getId()) {

            case APPEND_VALUE_REQUEST -> {
                AppendValueRequest request = GsonInstance.getInstance().getGson().fromJson(messageRequest.getContent(), AppendValueRequest.class);
             //   requestsMap.putOnRequestQueue(queueToClusterIdMap.get (request.getQueueId() ), messageRequest);

                requestsMap.putOnRequestQueue(assignToCluster(queueSequenceNumber,maxNumberOfClusters ) , messageRequest);

                return request.getClientId();
            }

            // La richiesta di creazione di una queue viene inviata al broker scelto secondo la logica "round robin"
            case CREATE_QUEUE_REQUEST -> {
                CreateQueueRequest request = GsonInstance.getInstance().getGson().fromJson(messageRequest.getContent(), CreateQueueRequest.class);
                request.setQueueID(generateNewQueueID());

                messageRequest.setContent(GsonInstance.getInstance().getGson().toJson(request)) ;
                if (!nextCluster.containsValue(0)) {
                    for (Integer clusterID : clustersID) {
                        nextCluster.put(clusterID, 0);
                    }
                }
                for (Integer clusterID : clustersID) {
                    if (nextCluster.get(clusterID) == 0) {
                        //requestsMap.putOnRequestQueue(clusterID, messageRequest);
                        requestsMap.putOnRequestQueue(assignToCluster(queueSequenceNumber,maxNumberOfClusters ) , messageRequest);

                        // queueToClusterIdMap.put(queueSequenceNumber, clusterID);
                        nextCluster.replace(clusterID, 1);
                        break;
                    }
                }
                return request.getClientId();
            }

            case READ_VALUE_REQUEST -> {
                ReadValueRequest request = GsonInstance.getInstance().getGson().fromJson(messageRequest.getContent(), ReadValueRequest.class);
               // requestsMap.putOnRequestQueue(queueToClusterIdMap.get (request.getQueueId() ), messageRequest);
                requestsMap.putOnRequestQueue(assignToCluster(queueSequenceNumber,maxNumberOfClusters ) , messageRequest);

                return request.getClientId();
            }
        }
        return null;
    }

    public void setUpConnectionWithNewLeader(SetUpConnectionMessage setUpConnectionMessage) throws IOException {
       // String clientId = messageResponse.getClientID();

        System.out.println("New leader: " + setUpConnectionMessage.toString());

                Integer clusterID = Integer.valueOf(setUpConnectionMessage.getClusterId());
                String ipAddress = setUpConnectionMessage.getHostName();
                Integer portNumber = setUpConnectionMessage.getPort();

                clustersID.add(clusterID);
                clusterIdToAddressMap.put(clusterID, ipAddress);
                clusterToPortMap.put(clusterID, portNumber);
                clusterConnected.put(clusterID, 1);
                requestsMap.addClusterID(clusterID);

            //    if (nextCluster.get(clusterID) == null )
                    nextCluster.put(clusterID, 0);

                connectionManager.startConnection(clusterID);

    }

    public boolean registerClientOnResponseMap(String clientId){
        if(responseMessageMap.isClientIdPresent(clientId)) {
            System.out.println("client already present");
            return false;
        }


        responseMessageMap.addClientId(clientId);
        System.out.println("registered client " +  clientId);
        return true;
    }

    public void putOnResponseMap(String clientId, MessageResponse messageResponse) {
       responseMessageMap.putOnResponseQueue(clientId, messageResponse);
    }

    public boolean newMessageOnQueue(String clientID) {
        //ritorna true se c'è un messaggio nella coda per quel client
        return !responseMessageMap.getMessageQueue(clientID).isEmpty();
    }

    private static int assignToCluster(int requestId, int totalClusters) {
        return requestId % totalClusters;
    }
}
