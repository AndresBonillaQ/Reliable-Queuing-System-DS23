package it.polimi.ds.broker.raft;

import it.polimi.ds.broker.raft.impl.RaftLog;

import java.util.List;

public interface IBrokerRaftIntegration {

    /**
     * Append Log to the Queue if Logs are consistency, otherwise throw LogBadRequestException
     * */
    void appendLog(List<RaftLog> raftLogs);

    /**
     * Used by Leader to create log to transmit to followers and increment the currentIndex by 1
     * */
    List<RaftLog> buildAndAppendNewLog(String request);

    List<RaftLog> appendNewLogAndTakeNotCommitted(String request);

    List<RaftLog> getRaftLogEntriesFromIndex(int from);

    void increaseCurrentTerm();

    int getCurrentTerm();

    int getCurrentIndex();

    int getPrevLogIndex();

    int getPrevLogTerm(int prevLogIndex);

    int getPrevLogTermOfIndex(int index);

    int getPrevLogIndexOf(int index);

    int getLastCommitIndex();

    void increaseCurrentIndex();

    void increaseLastCommitIndex();
    void increaseLastCommitIndex(int newLastCommitIndex);

    boolean isConsistent(int prevIndex, int prevTerm);

    List<RaftLog> getLogsToCommit(int lastCommitIndex);
}
