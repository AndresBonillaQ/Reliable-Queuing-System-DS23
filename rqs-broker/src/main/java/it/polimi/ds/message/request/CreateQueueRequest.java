package it.polimi.ds.message.request;

import java.io.Serializable;

public class CreateQueueRequest implements Serializable {
    private String clientId;
    private String queueId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}
