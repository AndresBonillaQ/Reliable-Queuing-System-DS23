package it.polimi.ds.utils.config;

import java.util.List;

public class BrokerConfig {

    private final String myBrokerId;

    private final String myClusterId;

    /**
     * Broker info which compose the cluster
     * */
    private final List<BrokerInfo> clusterBrokerConfig;

    private final GatewayInfo gatewayInfo;

    /**
     * The broker server port to follower
     * */
    private final int brokerServerPortToBrokers;

    /**
     * The broker server port to gateway
     * */
    private final int brokerServerPortToGateway;

    private final String myHostName;

    public BrokerConfig(String myBrokerId, String myClusterId, int brokerServerPortToBrokers, int brokerServerPortToGateway, List<BrokerInfo> clusterBrokerConfig, GatewayInfo gatewayInfo, String myHostName) {
        this.myBrokerId = myBrokerId;
        this.myClusterId = myClusterId;
        this.clusterBrokerConfig = clusterBrokerConfig;
        this.brokerServerPortToBrokers = brokerServerPortToBrokers;
        this.brokerServerPortToGateway = brokerServerPortToGateway;
        this.gatewayInfo = gatewayInfo;
        this.myHostName = myHostName;
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

    public String getMyBrokerId() {
        return myBrokerId;
    }

    public String getMyClusterId() {
        return myClusterId;
    }

    public GatewayInfo getGatewayInfo() {
        return gatewayInfo;
    }

    public String getMyHostName() {
        return myHostName;
    }
}
