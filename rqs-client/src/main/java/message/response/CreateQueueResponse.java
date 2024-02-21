package message.response;

import java.io.Serializable;

public class CreateQueueResponse extends Response implements Serializable {
    private String queueId;

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }
}
