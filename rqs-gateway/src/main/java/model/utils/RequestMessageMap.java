package model.utils;

import messages.MessageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestMessageMap {

    private Map<Integer, BlockingQueue<MessageRequest>> requestMap = new ConcurrentHashMap<>();//<clusterID, Coda dei messaggi da inviare al cluster con clusterUD>
    private ArrayList<Integer> clustersID = new ArrayList<>();



    public BlockingQueue<MessageRequest> getMessageQueue(Integer clusterID) {
        return requestMap.get(clusterID);
    }
    public void putOnRequestQueue(Integer clusterID, MessageRequest message) {
        requestMap.get(clusterID).add(message);
    }
    public void addClusterID(Integer clusterID) {
        if (!clustersID.contains(clusterID)) {
            clustersID.add(clusterID);
            requestMap.put(clusterID, new LinkedBlockingQueue<>());
        }
    }
}
