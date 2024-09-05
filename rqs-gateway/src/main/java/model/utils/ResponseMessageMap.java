package model.utils;

import messages.MessageResponse;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ResponseMessageMap {
    private final Map<String, BlockingQueue<MessageResponse>> responseMap = new ConcurrentHashMap<>();//<ClientId, Queue>

    public boolean isClientIdPresent(String clientId){
        return responseMap.containsKey(clientId);
    }

    public void addClientId(String clientId){
        responseMap.put(clientId, new LinkedBlockingQueue<>());
    }
    public void removeClientId(String clientId) {
        responseMap.remove(clientId);
    }

    public BlockingQueue<MessageResponse> getMessageQueue(String clientID) {
        return responseMap.get(clientID);
    }

    public void putOnResponseQueue(String clientId, MessageResponse message) {
        responseMap.get(clientId).add(message);
    }
}
