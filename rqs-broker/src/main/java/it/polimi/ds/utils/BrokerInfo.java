package it.polimi.ds.utils;

import java.net.InetSocketAddress;

public class BrokerInfo {
    private String brokerId;
    private String hostName;
    private int port;

    public BrokerInfo(String brokerId, String hostName, int port) {
        this.brokerId = brokerId;
        this.hostName = hostName;
        this.port = port;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
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
