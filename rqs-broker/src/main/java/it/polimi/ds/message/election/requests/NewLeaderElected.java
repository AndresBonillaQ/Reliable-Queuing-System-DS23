package it.polimi.ds.message.election.requests;

import it.polimi.ds.broker2.BrokerContext;

import java.io.Serializable;

public class NewLeaderElected implements Serializable {


    private BrokerContext brokerContext;

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }

    public void setBrokerContext(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
    }
}
