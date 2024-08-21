package it.polimi.ds.message.raft.request;

import java.io.Serializable;

public class HeartbeatRequest implements Serializable {
    private final String heartbeat = "Hi, I'm the leader!";
    private final String leaderId;

    public HeartbeatRequest(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HeartbeatRequest{");
        sb.append("heartbeat='").append(heartbeat).append('\'');
        sb.append(", leaderId='").append(leaderId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}