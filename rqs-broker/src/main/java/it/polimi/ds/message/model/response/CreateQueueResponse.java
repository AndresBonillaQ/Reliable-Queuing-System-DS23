package it.polimi.ds.message.model.response;

import it.polimi.ds.message.Response;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class CreateQueueResponse extends Response implements Serializable {
    private final String queueId;

    public CreateQueueResponse(StatusEnum statusEnum, String desStatus, String queueId) {
        this.status = statusEnum;
        this.desStatus = desStatus;
        this.queueId = queueId;
    }

    public String getQueueId() {
        return queueId;
    }
}
