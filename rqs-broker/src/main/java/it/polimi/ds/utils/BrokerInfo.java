package it.polimi.ds.utils;

import java.net.InetSocketAddress;

public class BrokerInfo {
    private String brokerId;
    private InetSocketAddress inetSocketAddress;

    public BrokerInfo(String brokerId, InetSocketAddress inetSocketAddress) {
        this.brokerId = brokerId;
        this.inetSocketAddress = inetSocketAddress;
    }

    public String getBrokerId() {
        return brokerId;
    }

    public void setBrokerId(String brokerId) {
        this.brokerId = brokerId;
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }
}
