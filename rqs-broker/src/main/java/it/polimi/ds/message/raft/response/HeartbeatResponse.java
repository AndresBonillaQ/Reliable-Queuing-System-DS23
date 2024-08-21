package it.polimi.ds.message.raft.response;

import it.polimi.ds.message.Response;

import java.io.Serializable;

public class HeartbeatResponse extends Response implements Serializable {
    private final String heartbeat = "Ok, Hi!";

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HeartbeatResponse{");
        sb.append("heartbeat='").append(heartbeat).append('\'');
        sb.append(", status=").append(status);
        sb.append(", desStatus='").append(desStatus).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
