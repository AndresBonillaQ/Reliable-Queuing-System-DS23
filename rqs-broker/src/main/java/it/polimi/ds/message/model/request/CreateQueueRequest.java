package it.polimi.ds.message.model.request;

import java.io.Serializable;

public class CreateQueueRequest implements Serializable {
    private String queueId;

    public CreateQueueRequest(String queueId) {
        this.queueId = queueId;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}
