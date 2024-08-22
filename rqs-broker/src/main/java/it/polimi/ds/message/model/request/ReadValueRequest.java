package it.polimi.ds.message.model.request;

import java.io.Serializable;

public class ReadValueRequest implements Serializable {
    private String clientId;
    private String queueId;

    public ReadValueRequest(String clientId, String queueId) {
        this.clientId = clientId;
        this.queueId = queueId;
    }

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
