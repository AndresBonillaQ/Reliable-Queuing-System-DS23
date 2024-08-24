package messages.responses;

import java.io.Serializable;

public class SetUpResponse extends Response implements Serializable {
    private final String clientId;

    public SetUpResponse(StatusEnum statusEnum, String desStatus, String clientId) {
        this.status = statusEnum;
        this.desStatus = desStatus;
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
