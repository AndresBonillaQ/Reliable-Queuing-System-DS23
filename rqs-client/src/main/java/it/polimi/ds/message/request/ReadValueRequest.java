package it.polimi.ds.message.request;

import java.io.Serializable;

public class ReadValueRequest implements Serializable {
    private String queueId;

    public ReadValueRequest(String queueId) {
        this.queueId = queueId;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}
