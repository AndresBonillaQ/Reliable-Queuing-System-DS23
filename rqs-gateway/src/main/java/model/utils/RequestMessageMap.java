package model.utils;

import messages.MessageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestMessageMap {

    private final Map<Integer, BlockingQueue<MessageRequest>> requestMap = new ConcurrentHashMap<>();//<clusterID, Coda dei messaggi da inviare al cluster con clusterUD>

    public BlockingQueue<MessageRequest> getMessageQueue(Integer clusterID) {
        return requestMap.get(clusterID);
    }

    public void putOnRequestQueue(Integer clusterID, MessageRequest message) {
        requestMap.get(clusterID).add(message);
    }

    public void addClusterID(Integer clusterID) {
        if (!requestMap.containsKey(clusterID)) {
            requestMap.put(clusterID, new LinkedBlockingQueue<>());
        }
    }

    public void removeClusterId(Integer clusterId){
        requestMap.remove(clusterId);
    }
}
