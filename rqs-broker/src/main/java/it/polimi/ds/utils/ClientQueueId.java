package it.polimi.ds.utils;

import java.util.Objects;

public class ClientQueueId {
    private final String clientId;
    private final String queueId;

    public ClientQueueId(String clientId, String queueId){
        this.clientId = clientId;
        this.queueId = queueId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getQueueId() {
        return queueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientQueueId that = (ClientQueueId) o;
        return Objects.equals(clientId, that.clientId) && Objects.equals(queueId, that.queueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, queueId);
    }
}
