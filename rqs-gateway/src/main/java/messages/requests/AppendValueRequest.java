package messages.requests;

import java.io.Serializable;



public class AppendValueRequest implements Serializable {
    private String queueId;
    private Integer value;

    public String getQueueId() {
        return queueId;
    }

    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
