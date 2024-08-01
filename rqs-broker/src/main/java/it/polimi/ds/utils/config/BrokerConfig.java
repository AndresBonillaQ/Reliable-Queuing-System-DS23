package it.polimi.ds.utils.config;

import java.net.InetSocketAddress;
import java.util.List;

public class BrokerConfig {

    /**
     * Number of brokers which compose the cluster
     * */
    private final List<InetSocketAddress> clusterBrokerAddresses;

    /**
     * The broker server port to follower
     * */
    private final int brokerServerPortToFollower;

    /**
     * The broker server port to gateway
     * */
    private final int brokerServerPortToGateway;

    public BrokerConfig(List<InetSocketAddress> clusterBrokerAddresses, int brokerServerPortToFollower, int brokerServerPortToGateway) {
        this.clusterBrokerAddresses = clusterBrokerAddresses;
        this.brokerServerPortToFollower = brokerServerPortToFollower;
        this.brokerServerPortToGateway = brokerServerPortToGateway;
    }

    public List<InetSocketAddress> getClusterBrokerAddresses() {
        return clusterBrokerAddresses;
    }

    public int getBrokerServerPortToFollower() {
        return brokerServerPortToFollower;
    }

    public int getBrokerServerPortToGateway() {
        return brokerServerPortToGateway;
    }
}
