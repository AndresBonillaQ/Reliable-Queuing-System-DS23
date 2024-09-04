package it.polimi.ds.broker.raft.persistence.utils;

import it.polimi.ds.broker.raft.utils.RaftLog;

import java.util.List;

public class RaftStateDto {
    private final List<RaftLog> raftLogQueue;
    private int currentTerm = 0;
    private int currentIndex = -1;
    private int lastCommitIndex = -1;
    private String myBrokerId;

    public RaftStateDto(List<RaftLog> raftLogQueue, int currentTerm, int currentIndex, int lastCommitIndex, String myBrokerId) {
        this.raftLogQueue = raftLogQueue;
        this.currentTerm = currentTerm;
        this.currentIndex = currentIndex;
        this.lastCommitIndex = lastCommitIndex;
        this.myBrokerId = myBrokerId;
    }

    public List<RaftLog> getRaftLogQueue() {
        return raftLogQueue;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getLastCommitIndex() {
        return lastCommitIndex;
    }

    public String getMyBrokerId() {
        return myBrokerId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RaftStateJson{");
        sb.append("raftLogQueue=").append(raftLogQueue);
        sb.append(", currentTerm=").append(currentTerm);
        sb.append(", currentIndex=").append(currentIndex);
        sb.append(", lastCommitIndex=").append(lastCommitIndex);
        sb.append(", myBrokerId='").append(myBrokerId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
