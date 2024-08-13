package it.polimi.ds.utils.config;

import it.polimi.ds.utils.BrokerInfo;

import java.util.List;

public class BrokerConfig {

    /**
     * Number of brokers which compose the cluster
     * */
    private final List<BrokerInfo> clusterBrokerConfig;

    /**
     * The broker server port to follower
     * */
    private final int brokerServerPortToBrokers;

    /**
     * The broker server port to gateway
     * */
    private final int brokerServerPortToGateway;

    public BrokerConfig(List<BrokerInfo> clusterBrokerConfig, int brokerServerPortToBrokers, int brokerServerPortToGateway) {
        this.clusterBrokerConfig = clusterBrokerConfig;
        this.brokerServerPortToBrokers = brokerServerPortToBrokers;
        this.brokerServerPortToGateway = brokerServerPortToGateway;
    }

    public List<BrokerInfo> getClusterBrokerConfig() {
        return clusterBrokerConfig;
    }

    public int getBrokerServerPortToBrokers() {
        return brokerServerPortToBrokers;
    }

    public int getBrokerServerPortToGateway() {
        return brokerServerPortToGateway;
    }
}
