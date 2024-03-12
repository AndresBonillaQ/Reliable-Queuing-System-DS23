package it.polimi.ds.broker.state;

import it.polimi.ds.broker.BrokerContext;

public abstract class BrokerState {

    protected final BrokerContext brokerContext;
    protected BrokerStateEnum brokerStateEnum;

    public BrokerState(BrokerContext brokerContext){
        this.brokerContext = brokerContext;
    }

    abstract public void exec();

    public void onHeartbeatTimeout(){}

    public void onWinLeaderElection(){}

    public void onLoseLeaderElection(){}

    public void onDiscoverLeaderWithHigherTerm(){} //?

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }

    public BrokerStateEnum getBrokerStateEnum() {
        return brokerStateEnum;
    }

    public void setBrokerStateEnum(BrokerStateEnum brokerStateEnum) {
        this.brokerStateEnum = brokerStateEnum;
    }
}
