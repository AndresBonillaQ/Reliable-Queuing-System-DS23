package messages.requests;

import java.io.Serializable;

public class CreateQueueRequest implements Serializable {
    private Integer queueId;

    public void setQueueId(Integer queueId) {
        this.queueId = queueId;
    }

    public Integer getQueueId() {
        return queueId;
    }
}