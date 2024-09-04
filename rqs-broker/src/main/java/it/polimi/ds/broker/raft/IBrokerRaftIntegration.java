package it.polimi.ds.broker.raft;

import it.polimi.ds.broker.raft.utils.RaftLog;
import it.polimi.ds.message.raft.request.RaftLogEntryRequest;

import java.util.List;
import java.util.Set;

public interface IBrokerRaftIntegration {

    /**
     * Used by Leader to create log to transmit to followers and increment the currentIndex by 1
     * */
    void buildAndAppendNewLog(String request);

    List<RaftLog> getLastUncommittedLogsToForward();

    List<RaftLog> getLastLogAppended();

    List<RaftLog> getRaftLogEntriesFromIndex(int from);

    void increaseCurrentTerm();
    void increaseCurrentTerm(int newCurrentTerm);

    int getCurrentTerm();

    int getPrevCommittedLogIndex();

    int getPrevLogTerm(int prevLogIndex);

    int getPrevLogTermOfIndex(int index);

    int getPrevLogIndexOf(int index);

    int getPrevLogIndex();

    int getLastCommitIndex();

    void printLogs();

    boolean processRaftLogEntryRequest(RaftLogEntryRequest request);

    List<String> processCommitRequestAndGetRequestsToExec(int lastCommitIndex);

    Set<String> calculateConsensus();

    void updateLogCommitState();

    List<RaftLog> getRaftLogQueue();

    String getMyBrokerId();

    int getCurrentIndex();

    List<String> getCommittedLogsToExec();

    List<String> getUncommittedLogsToExec();
}
