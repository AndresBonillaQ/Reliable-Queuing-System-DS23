package it.polimi.ds.message.raft.request;

import java.io.Serializable;

/**
 * Message sent by leader to followers to confirm operation and make them commit it
 * */
public class CommitLogRequest implements Serializable {
    private final int lastCommitIndex;

    public CommitLogRequest(int lastCommitIndex) {
        this.lastCommitIndex = lastCommitIndex;
    }

    public int getLastCommitIndex() {
        return lastCommitIndex;
    }
}
