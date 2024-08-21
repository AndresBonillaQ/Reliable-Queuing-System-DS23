package it.polimi.ds.message.election;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.broker2.model.IBrokerModel;

public class ElectionManager extends  Thread {

    private BrokerContext brokerContext;
    private Integer votesReceived;
    public ElectionManager(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        votesReceived = 0;
    }

    public synchronized void addVote() {
        votesReceived++;
    }
    public synchronized boolean isMajorityAchieved() {
        return votesReceived >= brokerContext.getNumClusterBrokers() / 2;
    }

    @Override
    public void run() {
        while (!isMajorityAchieved()) {

        }
    }
}
