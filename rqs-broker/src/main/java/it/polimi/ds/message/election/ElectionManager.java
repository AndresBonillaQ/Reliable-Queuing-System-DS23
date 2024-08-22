package it.polimi.ds.message.election;

import it.polimi.ds.broker.BrokerContext;

public class ElectionManager {

    private final BrokerContext brokerContext;
    private Integer votesReceived;

    public ElectionManager(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        votesReceived = 0;
    }

    public synchronized void addVote() {
        System.out.println("Adding vote!");
        votesReceived++;
    }
    public synchronized boolean isMajorityAchieved() {
        return votesReceived >= brokerContext.getNumClusterBrokers() / 2;
    }
}
