package it.polimi.ds.message.raft.response;

import java.io.Serializable;

/**
 * Message sent by followers to leader to confirm operation (consensus)
 * */
public class ConfirmOperationResponse implements Serializable {
    private final String response = "OPERATION_CONFIRMED!";
}
