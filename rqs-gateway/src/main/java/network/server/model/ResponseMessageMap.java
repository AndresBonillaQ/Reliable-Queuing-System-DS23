package network.server.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResponseMessageMap {
    private Map<String, BlockingQueue<String>> responseMap = new HashMap<>();//<ClientId, Queue>
    private ArrayList<String> clientIDlist = new ArrayList<>();



    public BlockingQueue<String> getMessageQueue(String clientID) {
        return responseMap.get(clientID);
    }
    public void putOnResponseQueue(String clientID, String message) {
        responseMap.get(clientID).add(message);
    }
    public void addClientID(String clientID) {
        if (!clientIDlist.contains(clientID)) {
            clientIDlist.add(clientID);
            responseMap.put(clientID, new LinkedBlockingQueue<>());
        }
    }
}
