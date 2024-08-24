package messages.requests;

import java.io.Serializable;

public class SetUpRequest implements Serializable {
    private final String clientId;

    public SetUpRequest(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
