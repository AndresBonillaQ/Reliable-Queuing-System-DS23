package it.polimi.ds.broker2.raft.impl;

import it.polimi.ds.broker2.raft.IBrokerRaftIntegration;
import it.polimi.ds.exception.raft.LogBadRequestException;
import it.polimi.ds.raftLog.RaftLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class contains the information about the Raft consensus protocol
 * */
public class BrokerRaftIntegration implements IBrokerRaftIntegration {

    /**
     * Contains broker log
     * */
    private final List<RaftLog> raftLogQueue = new ArrayList<>();

    /**
     * the currentTerm
     * */
    private int currentTerm = 0;

    /**
     * the currentIndex
     * */
    private int currentIndex = 0;

    /**
     * Append Log to the Queue if Logs are consistency, otherwise throw LogBadRequestException
     * */
    public void appendLog() throws LogBadRequestException {

    }

    /**
     * Used by Leader to create log to transmit to followers and increment the currentIndex by 1
     * */
    public synchronized List<RaftLog> buildAndAppendNewLog(String request){

        RaftLog raftLog = new RaftLog(currentTerm, ++currentIndex, request);
        raftLogQueue.add(raftLog);

        return List.of(raftLog);
    }

    public synchronized List<RaftLog> getRaftLogEntriesFromIndex(int from){
        if (from < 0 || from >= raftLogQueue.size())
            throw new IndexOutOfBoundsException("Index: " + from + ", Size: " + raftLogQueue.size()); //TODO

        return new ArrayList<>(raftLogQueue.subList(from, raftLogQueue.size()));
    }

    public synchronized void increaseCurrentTerm(){
        currentTerm++;
    }

    public synchronized int getCurrentTerm() {
        return currentTerm;
    }

    public synchronized int getCurrentIndex() {
        return currentIndex;
    }

    public synchronized int getPrevLogIndex(){
        if(currentIndex <= 0)
            return -1;
        return currentIndex - 1;
    }

    public synchronized int getPrevLogTerm(){
        if(currentTerm <= 0)
            return -1;
        return currentTerm - 1;
    }
}
