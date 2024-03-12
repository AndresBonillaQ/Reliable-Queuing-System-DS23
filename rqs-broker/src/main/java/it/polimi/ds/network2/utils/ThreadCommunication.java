package it.polimi.ds.network2.utils;

import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Managing communication between 1 thread (toGateway server) and N threads (toBroker client)
 * */
public class ThreadCommunication {
    private final ConcurrentHashMap<Socket, BlockingQueue<String>> requestConcurrentHashMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Socket, BlockingQueue<String>> responseConcurrentHashMap = new ConcurrentHashMap<>();

    private static ThreadCommunication instance;

    public static ThreadCommunication getInstance(){
        if(Objects.isNull(instance))
            instance = new ThreadCommunication();
        return instance;
    }

    public ConcurrentHashMap<Socket, BlockingQueue<String>> getRequestConcurrentHashMap() {
        return requestConcurrentHashMap;
    }

    public ConcurrentHashMap<Socket, BlockingQueue<String>> getResponseConcurrentHashMap() {
        return responseConcurrentHashMap;
    }

    public void addSocketQueue(Socket socket){
        requestConcurrentHashMap.put(socket, new LinkedBlockingQueue<>());
        responseConcurrentHashMap.put(socket, new LinkedBlockingQueue<>());
    }
}
