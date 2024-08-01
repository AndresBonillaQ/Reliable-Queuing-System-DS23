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
     * */
    private final ConcurrentHashMap<Socket, BlockingQueue<String>> requestConcurrentHashMap = new ConcurrentHashMap<>();

    /**
     * This Map contains, for each follower connection handler, a Blockingqueue containing
     * all messages received from follower
     * */
    private final ConcurrentHashMap<Socket, BlockingQueue<String>> responseConcurrentHashMap = new ConcurrentHashMap<>();

    private final Logger log = Logger.getLogger(ThreadsCommunication.class.getName());

    private ThreadsCommunication(){}

    private static ThreadsCommunication instance;

    public static ThreadsCommunication getInstance(){
        if(Objects.isNull(instance))
            instance = new ThreadsCommunication();
        return instance;
    }

    public void addSocketQueue(Socket socket){
        requestConcurrentHashMap.put(socket, new LinkedBlockingQueue<>());
        responseConcurrentHashMap.put(socket, new LinkedBlockingQueue<>());
    }

    public void addRequestToAllFollowerRequestQueue(String request){
        requestConcurrentHashMap.values().forEach(
                blockingQueue-> blockingQueue.add(request)
        );
    }

    public void addResponseToFollowerResponseQueue(Socket socket, String response){
        if(!Objects.isNull(responseConcurrentHashMap.get(socket)))
            responseConcurrentHashMap.get(socket).add(response);
        else
            log.log(Level.INFO, "The socket on port {0} is not associated to a responseQueue!", socket.getPort());

        log.log(Level.INFO, "ResponseBlockingQueue of socket with port {0} is {1}!", new Object[]{socket.getPort(), responseConcurrentHashMap.get(socket)});
    }

    public void onBrokerStateChange(){
        requestConcurrentHashMap.values().forEach(Collection::clear);
        responseConcurrentHashMap.values().forEach(Collection::clear);
    }

    public ConcurrentHashMap<Socket, BlockingQueue<String>> getResponseConcurrentHashMap() {
        return responseConcurrentHashMap;
    }

    public ConcurrentHashMap<Socket, BlockingQueue<String>> getRequestConcurrentHashMap() {
        return requestConcurrentHashMap;
    }
}
