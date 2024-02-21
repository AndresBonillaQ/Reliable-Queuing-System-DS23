package messages;

import java.io.Serializable;

public class CreateQueueResponse implements Serializable {
    private String queueId;
    private String status;
    private String desStatus;

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDesStatus() {
        return desStatus;
    }

    public void setDesStatus(String desStatus) {
        this.desStatus = desStatus;
    }
}
