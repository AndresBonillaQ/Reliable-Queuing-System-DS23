package it.polimi.ds.message.raft.response;

import it.polimi.ds.message.Response;

import java.io.Serializable;

public class HeartbeatResponse extends Response implements Serializable {
    private final String heartbeat = "Ok, Hi!";
}
