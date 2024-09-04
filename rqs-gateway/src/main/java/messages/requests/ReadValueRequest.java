package messages.requests;

import java.io.Serializable;

public class ReadValueRequest implements Serializable {
    private String queueId;

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}
