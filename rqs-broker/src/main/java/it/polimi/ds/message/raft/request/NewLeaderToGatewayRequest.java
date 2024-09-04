package it.polimi.ds.message.raft.request;

import java.io.Serializable;

public class NewLeaderToGatewayRequest implements Serializable {
    private final String clusterId;
    private final String leaderId;
    private final String hostName;
    private final int port;

    public NewLeaderToGatewayRequest(String clusterId, String leaderId, String hostName, int port) {
        this.clusterId = clusterId;
        this.leaderId = leaderId;
        this.hostName = hostName;
        this.port = port;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NewLeaderToGatewayRequest{");
        sb.append("clusterId='").append(clusterId).append('\'');
        sb.append(", leaderId='").append(leaderId).append('\'');
        sb.append(", hostName='").append(hostName).append('\'');
        sb.append(", port=").append(port);
        sb.append('}');
        return sb.toString();
    }
}
