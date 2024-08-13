package it.polimi.ds.network2.utils.thread.impl;

import java.net.Socket;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Managing communication between the one thread that receive messages from Gateway
 * and forward it to threads handling communication with followers
 * */
public class ThreadsCommunication {

    /**
     * This Map contains, for each follower connection handler, a Blockingqueue containing
     * all messages to send to follower
     * @key is the brokerId
     * */
    private final ConcurrentHashMap<String, BlockingQueue<String>> requestConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * This Map contains, for each follower connection handler, a Blockingqueue containing
     * all messages received from follower
     * @key is the brokerId
     * */
    private final ConcurrentHashMap<String, BlockingQueue<String>> responseConcurrentHashMap = new ConcurrentHashMap<>();

    private final Logger log = Logger.getLogger(ThreadsCommunication.class.getName());

    private ThreadsCommunication(){}

    private static ThreadsCommunication instance;

    public static ThreadsCommunication getInstance(){
        if(Objects.isNull(instance))
            instance = new ThreadsCommunication();
        return instance;
    }

    public boolean addBrokerId(String brokerId){
        log.log(Level.INFO, "Adding brokerId {0} to requestConcurrentHashMap", brokerId);

        if(requestConcurrentHashMap.containsKey(brokerId) || responseConcurrentHashMap.containsKey(brokerId))
            return false;

        requestConcurrentHashMap.put(brokerId, new LinkedBlockingQueue<>());
        responseConcurrentHashMap.put(brokerId, new LinkedBlockingQueue<>());

        return true;
    }

    public void addRequestToAllFollowerRequestQueue(String request){
        log.log(Level.INFO, "Adding request {0} to requestConcurrentHashMap with size {1}", new Object[]{request, requestConcurrentHashMap.size()});
        requestConcurrentHashMap.values().forEach(
                blockingQueue-> blockingQueue.add(request)
        );
    }

    public void addResponseToFollowerResponseQueue(String brokerId, String response){
        if(!Objects.isNull(responseConcurrentHashMap.get(brokerId)))
            responseConcurrentHashMap.get(brokerId).add(response);
        else
            log.log(Level.INFO, "The brokerId {0} is not associated to a responseQueue!", brokerId);

        log.log(Level.INFO, "ResponseBlockingQueue is {1}!", responseConcurrentHashMap.get(brokerId));
    }

    public void onBrokerStateChange(){
        requestConcurrentHashMap.values().forEach(Collection::clear);
        responseConcurrentHashMap.values().forEach(Collection::clear);
    }

    public ConcurrentHashMap<String, BlockingQueue<String>> getResponseConcurrentHashMap() {
        return responseConcurrentHashMap;
    }

    public BlockingQueue<String> getRequestConcurrentHashMapOfBrokerId(String brokerId) {
        log.log(Level.INFO, "Retrieving BlockingQueue of brokerId {0}, isPresent {1}", new Object[]{brokerId, requestConcurrentHashMap.containsKey(brokerId)});

        return requestConcurrentHashMap.get(brokerId);
    }
}
