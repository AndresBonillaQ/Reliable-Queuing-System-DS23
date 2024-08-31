package it.polimi.ds.message.raft.response;

import it.polimi.ds.message.Response;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class HeartbeatResponse extends Response implements Serializable {
    private final String heartbeat = "Ok, Hi!";

    public HeartbeatResponse(StatusEnum statusEnum, String desStatus) {
        this.status = statusEnum;
        this.desStatus = desStatus;
    }

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
