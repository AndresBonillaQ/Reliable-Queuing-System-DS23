package it.polimi.ds.message.request;

import java.io.Serializable;

public class SetUpRequest implements Serializable {
    private final String clientId;

    public SetUpRequest(String clientId) {
        this.clientId = clientId;
    }
}
