package it.polimi.ds.broker.raft.impl;

import it.polimi.ds.broker.raft.IBrokerRaftIntegration;
import it.polimi.ds.message.raft.request.RaftLogEntryRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class contains the information about the Raft consensus protocol
 * Term starts from 0
 * Index starts from -1
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
    private int currentIndex = -1;

    /**
     * the latest log index committed
     * */
    private int lastCommitIndex = -1;

    private final Logger log = Logger.getLogger(BrokerRaftIntegration.class.getName());

    /**
     * Append Log to the Queue if Logs are consistency
     * When received request from leader as follower
     * */
    public void appendLog(int prevLogIndex, List<RaftLog> raftLogs) {

        if(raftLogQueue.size() <= 1 && prevLogIndex == -1){
            raftLogQueue.addAll(raftLogs);
            currentIndex += raftLogs.size();
        } else if (raftLogQueue.size() >= 2 && prevLogIndex == raftLogQueue.size() - 2) {
            raftLogQueue.addAll(raftLogs);
            currentIndex += raftLogs.size();
        }

    }

    /**
     * Used by Leader to create log to transmit to followers and increment the currentIndex by 1
     * When received request from gateway as Leader
     * */
    public List<RaftLog> buildAndAppendNewLog(String request){

        RaftLog raftLog = new RaftLog(currentTerm, request);
        raftLogQueue.add(raftLog);
        currentIndex++;

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

    public void increaseCurrentTerm(int newCurrentTerm){
        currentTerm = newCurrentTerm;
    }

    public synchronized int getCurrentTerm() {
        return currentTerm;
    }

    public synchronized int getCurrentIndex() {
        return currentIndex;
    }

    public synchronized int getPrevLogIndex(){
        return Math.max(-1, currentIndex - 1);
    }

    public synchronized int getPrevLogIndexOf(int index){
        return Math.max(-1, index - 1);
    }

    public synchronized int getPrevLogTerm(){
        if(currentTerm <= 0)
            return 0;
        return currentTerm - 1;
    }

    public int getPrevLogTerm(int prevLogIndex){
        if(prevLogIndex < 0)
            return 0;

        return raftLogQueue.get(prevLogIndex).getTerm();
    }

    public int getPrevLogTermOfIndex(int index){
        if(index - 1 < 0)
            return 0;

        return raftLogQueue.get(index - 1).getTerm();
    }

    public void increaseLastCommitIndex(){
        lastCommitIndex++;
    }

    public void increaseLastCommitIndex(int newLastCommitIndex){
        lastCommitIndex = newLastCommitIndex;
    }

    public void increaseCurrentIndex(){
        currentIndex++;
    };

    public int getLastCommitIndex() {
        return lastCommitIndex;
    }

    /**
     * @param prevIndex must be equal to index of last log entry
     * @param prevTerm must be equal to term of last log entry
     * */
    public boolean isConsistent(int prevIndex, int prevTerm){
        log.log(Level.INFO, "Checking consistency of prevIndex: {0} and prevTerm : {1}, raftLogQueue is {2}", new Object[]{prevIndex, prevTerm, raftLogQueue});

        if(raftLogQueue.isEmpty())
            return prevIndex == -1 && prevTerm == 0;

        RaftLog lastRaftLog = raftLogQueue.get(raftLogQueue.size() - 1);

        return raftLogQueue.size() - 1 == prevIndex && lastRaftLog.getTerm() == prevTerm;
    }

    public List<RaftLog> getLogsToCommit(int newLastCommitIndex){
        return raftLogQueue.subList(this.lastCommitIndex + 1, newLastCommitIndex + 1);
    }

    public void printLogs(){
        System.out.println("------------------------------------ RAFT LOG QUEUE ------------------------------------------------");
        for(RaftLog log : raftLogQueue){
            System.out.println(raftLogQueue.indexOf(log) + ": " + log);
        }
        System.out.format("CurrentTerm: %s, CurrentIndex: %s, LastCommitIndex: %s\n", currentTerm, currentIndex, lastCommitIndex);
        System.out.println("----------------------------------------------------------------------------------------------------");
    }

    public int getLogQueueSize(){
        return raftLogQueue.size();
    }

    public boolean processRaftLogEntryRequest(RaftLogEntryRequest request){
        log.log(Level.INFO, "Processing request {0}", request);

        if(isConsistent(request.getPrevLogIndex(), request.getPrevLogTerm(), request.getTerm())){
            log.log(Level.INFO, "LogRequest is consistent");

            mergeNewLogEntries(request.getPrevLogIndex(), request.getRafLogEntries());
            currentIndex = raftLogQueue.size() - 1;

            return true;
        }

        return false;
    }

    private boolean isConsistent(int prevIndex, int prevTerm, int requestTerm) {

        log.log(Level.INFO, "Checking consistency of prevIndex:{0}, prevTerm:{1}, requestTerm:{2}", new Object[]{prevIndex, prevTerm, requestTerm});

        if(prevIndex < -1 || prevTerm < 0){
            log.log(Level.SEVERE, "Impossible to manage request");
            return false;
        }

        if(requestTerm < currentTerm){
            log.log(Level.SEVERE, "Term of request {0} is lower than currentTerm of broker {1}", new Object[]{requestTerm, currentTerm});
            return false;
        }

        if(raftLogQueue.isEmpty()){
            log.log(Level.SEVERE, "RaftLogQueue is empty and prevIndex:{0}, prevTerm:{1}", new Object[]{prevIndex, prevTerm});
            return prevIndex == - 1 && prevTerm == 0;
        }

        if(prevIndex >= raftLogQueue.size()){
            log.log(Level.SEVERE, "Impossible to append Log due to inconsistency, prevIndex {0} is greater than logQueueSize {1}", new Object[]{prevIndex, raftLogQueue.size()});
            return false;
        }

        RaftLog raftLogToCheck = raftLogQueue.get(prevIndex);
        log.log(Level.INFO, "Checking consistency of term of log {0}", raftLogToCheck);

        return raftLogToCheck.getTerm() == prevTerm;
    }

    /**
     * This method is used to merge new logs as Follower
     * */
    private void mergeNewLogEntries(int prevIndex, List<RaftLog> logsToAppend){

        int offset = Math.min(raftLogQueue.size(), prevIndex + 1);

        raftLogQueue.subList(offset, raftLogQueue.size()).clear();

        for(int i = offset; i < logsToAppend.size(); i++)
            raftLogQueue.set(i, logsToAppend.get(i));

    }

    public List<String> processCommitRequestAndGetRequestsToExec(int newLastCommitIndex){

        if (newLastCommitIndex < this.lastCommitIndex || newLastCommitIndex >= raftLogQueue.size()) {
            throw new IndexOutOfBoundsException("newLastCommitIndex: " + newLastCommitIndex + " is out of bounds."); //TODO catch
        }

        List<RaftLog> raftLogsToCommit = raftLogQueue.subList(this.lastCommitIndex + 1, newLastCommitIndex + 1);
        raftLogsToCommit.forEach(log -> log.setCommitted(true));
        this.lastCommitIndex = newLastCommitIndex;

        return raftLogsToCommit.stream().map(RaftLog::getRequest).collect(Collectors.toList());
    }

    public void commitLastLogAppended(){
        raftLogQueue.get(raftLogQueue.size() - 1).setCommitted(true);
    }
}
