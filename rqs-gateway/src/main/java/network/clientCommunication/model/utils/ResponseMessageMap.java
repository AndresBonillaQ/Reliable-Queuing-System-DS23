package network.clientCommunication.model.utils;

import messages.MessageResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResponseMessageMap {
    private Map<String, BlockingQueue<MessageResponse>> responseMap = new HashMap<>();//<ClientId, Queue>
    private ArrayList<String> clientIDlist = new ArrayList<>();



    public BlockingQueue<MessageResponse> getMessageQueue(String clientID) {
        return responseMap.get(clientID);
    }
    public void putOnResponseQueue(String clientID, MessageResponse message) {
        responseMap.get(clientID).add(message);
    }
}
