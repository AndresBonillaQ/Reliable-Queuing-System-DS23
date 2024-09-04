package model;

import exception.NoClusterAvailableException;
import messages.MessageRequest;
import messages.MessageResponse;
import messages.connectionSetUp.SetUpConnectionMessage;
import messages.id.ResponseIdEnum;
import messages.requests.AppendValueRequest;
import messages.requests.CreateQueueRequest;
import messages.requests.ReadValueRequest;
import messages.responses.ServiceUnavailableResponse;
import messages.responses.StatusEnum;
import network.brokerCommunication.client.ConnectionManager;
import model.utils.RequestMessageMap;
import model.utils.ResponseMessageMap;
import utils.GsonInstance;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
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
    private HashMap<Integer, Integer> clusterConnected = new HashMap<>();
    private HashMap<Integer, Socket> clusterToSocketMap = new HashMap();

    private static int maxNumberOfClusters = 0;
    private static ConnectionManager connectionManager;
    private int queueSequenceNumber = -1 ;
    private int clientIdSequenceNumber = -1 ;


    public static Gateway getInstance() {
        if (instance == null) {
            instance = new Gateway();
        }

        if (connectionManager == null )
            connectionManager =  new ConnectionManager();

        return instance;
    }

    public Integer generateNewClientID() {
        clientIdSequenceNumber++;
        return clientIdSequenceNumber;
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


    public String processRequest(PrintWriter outputStream, MessageRequest messageRequest) throws IOException {

        switch (messageRequest.getId()) {

            case APPEND_VALUE_REQUEST -> {

                AppendValueRequest request = GsonInstance.getInstance().getGson().fromJson(messageRequest.getContent(), AppendValueRequest.class);

                if(requestsMap.getMessageQueue(assignToCluster(Integer.parseInt(request.getQueueId()))) != null ){
                    requestsMap.putOnRequestQueue(assignToCluster(Integer.parseInt(request.getQueueId())) , messageRequest);

                    return messageRequest.getClientId();
                } else
                    sendServiceUnavailableResponse(messageRequest.getClientId(), outputStream);
            }

            // La richiesta di creazione di una queue viene inviata al broker scelto secondo la logica "round robin"
            case CREATE_QUEUE_REQUEST -> {
                CreateQueueRequest request = GsonInstance.getInstance().getGson().fromJson(messageRequest.getContent(), CreateQueueRequest.class);

                try {

                    queueSequenceNumber = getQueueIdOfClusterAvailable();
                    request.setQueueId(queueSequenceNumber);

                    messageRequest.setContent(GsonInstance.getInstance().getGson().toJson(request)) ;
                    requestsMap.putOnRequestQueue(assignToCluster(queueSequenceNumber) , messageRequest);
                    return messageRequest.getClientId();

                } catch (NoClusterAvailableException e) {
                    sendServiceUnavailableResponse(messageRequest.getClientId(), outputStream);
                }
            }

            case READ_VALUE_REQUEST -> {
                ReadValueRequest request = GsonInstance.getInstance().getGson().fromJson(messageRequest.getContent(), ReadValueRequest.class);

                if(requestsMap.getMessageQueue(assignToCluster(Integer.parseInt(request.getQueueId()))) != null)
                    requestsMap.putOnRequestQueue(assignToCluster(Integer.parseInt(request.getQueueId())) , messageRequest);
                else
                    sendServiceUnavailableResponse(messageRequest.getClientId(), outputStream);

                return messageRequest.getClientId();
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
                System.out.println("Added cluster: " + clusterID);
                connectionManager.startConnection(clusterID);

    }

    public boolean registerClientOnResponseMap(String clientId){
        responseMessageMap.addClientId(clientId);
        return true;
    }
    public boolean isClientPresent(String clientId) {
        return responseMessageMap.isClientIdPresent(clientId);
    }

    public void putOnResponseMap(String clientId, MessageResponse messageResponse) {
       responseMessageMap.putOnResponseQueue(clientId, messageResponse);
    }

    public boolean newMessageOnQueue(String clientID) {
        //ritorna true se c'è un messaggio nella coda per quel client
        return !responseMessageMap.getMessageQueue(clientID).isEmpty();
    }

    public void removeFromRequestMap(Integer clusterId){
        requestsMap.removeClusterId(clusterId);
    }

    private static int assignToCluster(int requestId) {
        return requestId % maxNumberOfClusters;
    }

    private void sendServiceUnavailableResponse(String clientId, PrintWriter out){
        ServiceUnavailableResponse serviceUnavailableResponse = new ServiceUnavailableResponse(StatusEnum.KO, "Service temporarily unavailable");

        MessageResponse messageResponse = new MessageResponse(
                ResponseIdEnum.SERVICE_UNAVAILABLE_RESPONSE,
                GsonInstance.getInstance().getGson().toJson(serviceUnavailableResponse),
                clientId
        );

        out.println(GsonInstance.getInstance().getGson().toJson(messageResponse));
        out.flush();
    }

    /**
     * Give the clusterId associated to the queueId
     * */
    private int getQueueIdOfClusterAvailable() throws NoClusterAvailableException{

        int tmpQueueId = queueSequenceNumber;
        for(int i = 0; i < maxNumberOfClusters; i++){
            tmpQueueId++;
            if(requestsMap.getMessageQueue(assignToCluster(tmpQueueId)) != null)
                return tmpQueueId;
        }

        throw new NoClusterAvailableException();
    }

    public HashMap<Integer, Socket> getClusterToSocketMap() {
        return clusterToSocketMap;
    }

    public void putOnSocketMap(Integer clusterId, Socket socket) {
        this.clusterToSocketMap.put(clusterId, socket);
    }
    public void removeClientId(String clientId) {
        responseMessageMap.removeClientId(clientId);
    }

    public void setMaxNumberOfClusters(int value){
        maxNumberOfClusters = value;
    }
}
