package network.clientCommunication.model.utils;

import messages.MessageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestMessageMap {

    private Map<String, BlockingQueue<MessageRequest>> requestMap = new HashMap<>();//<ClientId, Queue>
    private ArrayList<String> clustersID = new ArrayList<>();



    public BlockingQueue<MessageRequest> getMessageQueue(String clusterID) {
        return requestMap.get(clusterID);
    }
    public void putOnRequestQueue(String clusterID, MessageRequest message) {
        requestMap.get(clusterID).add(message);
    }
    public void addClusterID(String clusterID) {
        if (!clustersID.contains(clusterID)) {
            clustersID.add(clusterID);
            requestMap.put(clusterID, new LinkedBlockingQueue<>());
        }
    }
}
