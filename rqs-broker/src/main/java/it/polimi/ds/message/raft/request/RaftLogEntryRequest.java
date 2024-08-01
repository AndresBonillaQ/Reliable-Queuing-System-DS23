package it.polimi.ds.message.raft.request;

import it.polimi.ds.raftLog.RaftLog;

import java.io.Serializable;
import java.util.List;

public class RaftLogEntryRequest implements Serializable {
    private final int term;
    private final String leaderId;
    private final int prevLogIndex;
    private final int prevLogTerm;
    private final int leaderCommit; //??
    private final List<RaftLog> rafLogEntries;

    public RaftLogEntryRequest(int term, String leaderId, int prevLogIndex, int prevLogTerm, int leaderCommit, List<RaftLog> rafLogEntries) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.leaderCommit = leaderCommit;
        this.rafLogEntries = rafLogEntries;
    }

    public int getTerm() {
        return term;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }

    public List<RaftLog> getRafLogEntries() {
        return rafLogEntries;
    }
}
