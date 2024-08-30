package messages.requests;

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

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getQueueId() {
        return queueId;
    }
}