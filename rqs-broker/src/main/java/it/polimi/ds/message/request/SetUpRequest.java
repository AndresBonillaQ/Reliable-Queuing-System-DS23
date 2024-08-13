package it.polimi.ds.message.request;

import java.io.Serializable;

public class SetUpRequest implements Serializable {
    private String brokerId;

    public SetUpRequest(String brokerId) {
        this.brokerId = brokerId;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }
}
