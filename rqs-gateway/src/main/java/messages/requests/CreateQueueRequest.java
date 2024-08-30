package messages.requests;

import java.io.Serializable;

public class CreateQueueRequest implements Serializable {
    private String clientId;
    private Integer queueID;
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setQueueID(Integer queueID) {
        this.queueID = queueID;
    }

    public Integer getQueueID() {
        return queueID;
    }
}