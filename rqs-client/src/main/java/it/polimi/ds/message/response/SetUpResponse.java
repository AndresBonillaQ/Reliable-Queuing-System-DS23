package it.polimi.ds.message.response;

import java.io.Serializable;

public class SetUpResponse extends Response implements Serializable {
    private final String clientId;

    public SetUpResponse(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }
}
