package messages.connectionSetUp;

import java.io.Serializable;

public class SetUpConnectionMessage implements Serializable {
    private String hostName;
    private Integer port;
    private String clusterId;
    private String leaderId;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SetUpConnectionMessage{");
        sb.append("hostName='").append(hostName).append('\'');
        sb.append(", port=").append(port);
        sb.append(", clusterId='").append(clusterId).append('\'');
        sb.append(", leaderId='").append(leaderId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
