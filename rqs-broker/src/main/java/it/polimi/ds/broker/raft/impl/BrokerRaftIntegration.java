package it.polimi.ds.broker.raft.impl;

import it.polimi.ds.broker.raft.IBrokerRaftIntegration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
    private int currentIndex = -1;

    /**
     * the latest log index committed
     * */
    private int lastCommitIndex = -1;

    private final Logger log = Logger.getLogger(BrokerRaftIntegration.class.getName());

    /**
     * Append Log to the Queue if Logs are consistency, otherwise throw LogBadRequestException
     * When received request from leader as follower
     * */
    public void appendLog(List<RaftLog> raftLogs) {
        raftLogQueue.addAll(raftLogs);
        currentIndex += raftLogs.size();

        printLogs();
    }

    /**
     * Used by Leader to create log to transmit to followers and increment the currentIndex by 1
     * When received request from gateway as Leader
     * */
    public List<RaftLog> buildAndAppendNewLog(String request){

        RaftLog raftLog = new RaftLog(currentTerm, request);
        raftLogQueue.add(raftLog);
        currentIndex++;

        printLogs();

        return List.of(raftLog);
    }

    // non necessario con la logica di retry quando prev non coincide
    public List<RaftLog> appendNewLogAndTakeNotCommitted(String request){

        RaftLog raftLog = new RaftLog(currentTerm, request);

        raftLogQueue.add(raftLog);
        currentIndex++;

        return raftLogQueue.subList(lastCommitIndex + 1, raftLogQueue.size() - 1);
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
        if(currentIndex < 0)
            return -1;
        return currentIndex - 1;
    }

    public synchronized int getPrevLogIndexOf(int index){
        if(index < 0)
            return -1;

        return index - 1;
    }

    public synchronized int getPrevLogTerm(){
        if(currentTerm <= 0)
            return 0;
        return currentTerm - 1;
    }

    public synchronized int getPrevLogTermOfIndex(int index){
        return raftLogQueue.get(index).getTerm();
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
        if(raftLogQueue.isEmpty())
            return prevIndex == -1 && prevTerm == 0;

        RaftLog lastRaftLog = raftLogQueue.get(raftLogQueue.size() - 1);

        return raftLogQueue.size() - 1 == prevIndex && lastRaftLog.getTerm() == prevTerm;
    }

    public List<RaftLog> getLogsToCommit(int newLastCommitIndex){
        System.out.format("Committing from %s to %s in queue with size %s\n", new Object[]{this.lastCommitIndex + 1, newLastCommitIndex + 1, raftLogQueue.size()});
        return raftLogQueue.subList(this.lastCommitIndex + 1, newLastCommitIndex + 1);
    }

    private void printLogs(){
        System.out.println("+------+----------+-----------+");
        System.out.println("| Term | Request  | Committed |");
        System.out.println("+------+----------+-----------+");

        for (RaftLog log : raftLogQueue) {
            System.out.printf("| %-4d | %-67s | %-9s |\n",
                    log.getTerm(),
                    log.getRequest(),
                    log.isCommitted() ? "Yes" : "No");
        }

        System.out.println("+------+----------+-----------+");

        System.out.println("======================================================================================================================");
        System.out.format("currentTerm: %d, currentIndex: %d \n", new Object[]{currentTerm, currentIndex});
        System.out.println("---------------------------------------------------------------------------------------------------------------------");
    }
}
