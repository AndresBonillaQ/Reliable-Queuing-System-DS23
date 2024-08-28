package model.utils;

import messages.MessageResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResponseMessageMap {
    private final Map<String, BlockingQueue<MessageResponse>> responseMap = new HashMap<>();//<ClientId, Queue>

    public boolean isClientIdPresent(String clientId){
        return responseMap.containsKey(clientId);
    }

    public void addClientId(String clientId){
        responseMap.put(clientId, new LinkedBlockingQueue<>());
    }

    public BlockingQueue<MessageResponse> getMessageQueue(String clientID) {
        return responseMap.get(clientID);
    }

    public void putOnResponseQueue(String clientId, MessageResponse message) {
        System.out.println("Il clientId é: " + clientId);
        responseMap.get(clientId).add(message);
    }
}
