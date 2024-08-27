package it.polimi.ds.message.raft.request;

import java.io.Serializable;

public class HeartbeatRequest implements Serializable {
    private final String heartbeat = "Hi, I'm the leader!";
    private final String leaderId;
    private final int term;

    public HeartbeatRequest(String leaderId, int term) {
        this.leaderId = leaderId;
        this.term = term;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public int getTerm() {
        return term;
    }

    public String getHeartbeat() {
        return heartbeat;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HeartbeatRequest{");
        sb.append("heartbeat='").append(heartbeat).append('\'');
        sb.append(", leaderId='").append(leaderId).append('\'');
        sb.append(", term=").append(term);
        sb.append('}');
        return sb.toString();
    }
}