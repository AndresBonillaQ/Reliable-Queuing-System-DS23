package it.polimi.ds.network2.utils;

import java.io.Serializable;

public class LeaderWaitingForFollowersResponse implements Serializable {
    private final String brokerId;
    private final String response;

    public LeaderWaitingForFollowersResponse(String brokerId, String response) {
        this.brokerId = brokerId;
        this.response = response;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public String getResponse() {
        return response;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LeaderWaitingForFollowersResponse{");
        sb.append("brokerId='").append(brokerId).append('\'');
        sb.append(", response='").append(response).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
