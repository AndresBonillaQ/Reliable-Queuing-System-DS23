package it.polimi.ds.broker2.raft;

import it.polimi.ds.exception.raft.LogBadRequestException;
import it.polimi.ds.raftLog.RaftLog;

import java.util.ArrayList;
import java.util.List;

public interface IBrokerRaftIntegration {

    /**
     * Append Log to the Queue if Logs are consistency, otherwise throw LogBadRequestException
     * */
    void appendLog() throws LogBadRequestException;

    /**
     * Used by Leader to create log to transmit to followers and increment the currentIndex by 1
     * */
    List<RaftLog> buildAndAppendNewLog(String request);

    List<RaftLog> getRaftLogEntriesFromIndex(int from);

    void increaseCurrentTerm();

    int getCurrentTerm();

    int getCurrentIndex();

    int getPrevLogIndex();

    int getPrevLogTerm();
}
