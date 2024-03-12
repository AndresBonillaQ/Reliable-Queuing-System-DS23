package it.polimi.ds.message.response;

import java.io.Serializable;

public class HeartbeatResponse extends Response implements Serializable {
    private final String heartbeat = "Ok, Hi!";
}
