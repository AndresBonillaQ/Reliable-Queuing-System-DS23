package messages.responses;

import java.io.Serializable;

public class CreateQueueResponse implements Serializable {
    private String queueId;

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}
