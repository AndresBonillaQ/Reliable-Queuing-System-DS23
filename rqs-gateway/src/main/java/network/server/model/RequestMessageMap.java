package network.server.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestMessageMap {

    private Map<String, BlockingQueue<String>> requestMap = new HashMap<>();//<ClientId, Queue>

   /* public RequestMessageMap() throws IOException {
        for (String clusterID: GateWay.getInstance().getClusterID()
             ) {
            requestMap.put(clusterID, new LinkedBlockingQueue<>());
        }
    }*/
    public void addClusterID(String clusterID) {
        requestMap.put(clusterID, new LinkedBlockingQueue<>());
    }


    public BlockingQueue<String> getMessageQueue(String clusterID) {
        return requestMap.get(clusterID);
    }
    public void putOnRequestQueue(String clusterID, String message) {
        requestMap.get(clusterID).add(message);

    }
}
