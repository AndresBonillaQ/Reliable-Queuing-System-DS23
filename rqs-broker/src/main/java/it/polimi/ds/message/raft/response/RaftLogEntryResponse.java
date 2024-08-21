package it.polimi.ds.message.raft.response;

import it.polimi.ds.message.Response;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class RaftLogEntryResponse extends Response implements Serializable {
    private final int lastMatchIndex;

    public RaftLogEntryResponse(int lastMatchIndex, StatusEnum status, String desStatus) {
        this.lastMatchIndex = lastMatchIndex;
        this.status = status;
        this.desStatus = desStatus;
    }

    public int getLastMatchIndex() {
        return lastMatchIndex;
    }
}
