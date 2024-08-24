package it.polimi.ds.message.election;

import it.polimi.ds.broker.BrokerContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ElectionManager {

    private final BrokerContext brokerContext;
    private Integer votesReceived;
    private final Logger log = Logger.getLogger(ElectionManager.class.getName());

    public ElectionManager(BrokerContext brokerContext) {
        this.brokerContext = brokerContext;
        votesReceived = 1;  //vote myself
    }

    public synchronized void addVote() {
        log.log(Level.INFO, "Adding vote!");
        votesReceived++;
    }

    public synchronized boolean isMajorityAchieved() {
        return votesReceived > brokerContext.getNumClusterBrokers() / 2;
    }
}
