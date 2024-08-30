package messages.requests;

import java.io.Serializable;

public class CreateQueueRequest implements Serializable {
    private String clientId;
    private Integer queueId;
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public Integer getQueueId() {
        return queueID;
    }
}