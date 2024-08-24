package messages.requests;

import java.io.Serializable;

public class CreateQueueRequest implements Serializable {
    private String clientId;
    private String queueID;
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setQueueID(String queueID) {
        this.queueID = queueID;
    }

    public String getQueueID() {
        return queueID;
    }
}