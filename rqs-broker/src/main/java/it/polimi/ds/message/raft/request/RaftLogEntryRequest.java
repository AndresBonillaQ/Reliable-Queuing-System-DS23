package it.polimi.ds.message.raft.request;

import it.polimi.ds.broker.raft.impl.RaftLog;

import java.io.Serializable;
import java.util.List;

public class RaftLogEntryRequest implements Serializable {
    private final int term;
    private final String leaderId;
    private final int prevLogIndex;
    private final int prevLogTerm;
    private final List<RaftLog> raftLogs;

    public RaftLogEntryRequest(int term, String leaderId, int prevLogIndex, int prevLogTerm, List<RaftLog> raftLogs) {
        this.term = term;
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.raftLogs = raftLogs;
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

    public List<RaftLog> getRafLogEntries() {
        return raftLogs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RaftLogEntryRequest{");
        sb.append("term=").append(term);
        sb.append(", leaderId='").append(leaderId).append('\'');
        sb.append(", prevLogIndex=").append(prevLogIndex);
        sb.append(", prevLogTerm=").append(prevLogTerm);
        sb.append(", raftLogs=").append(raftLogs);
        sb.append('}');
        return sb.toString();
    }
}
