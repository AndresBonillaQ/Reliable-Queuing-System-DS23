package messages;

import java.io.Serializable;

public class CreateQueueRequest implements Serializable {
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
