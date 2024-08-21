package it.polimi.ds.utils.config;

public class BrokerInfo {
    private String clientBrokerId;
    private String hostName;
    private int port;

    public BrokerInfo(String clientBrokerId, String hostName, int port) {
        this.clientBrokerId = clientBrokerId;
        this.hostName = hostName;
        this.port = port;
    }

    public String getClientBrokerId() {
        return clientBrokerId;
    }

    public void setClientBrokerId(String clientBrokerId) {
        this.clientBrokerId = clientBrokerId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
