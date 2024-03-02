package it.polimi.ds.message.raft.request;

import java.io.Serializable;

/**
 * Message sent by leader to followers to confirm operation and make them commit it
 * */
public class CommitOperationRequest implements Serializable {
    private final String request = "COMMIT_OPERATION!";
}
