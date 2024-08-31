package it.polimi.ds.broker.raft;

import it.polimi.ds.broker.BrokerContext;

import java.util.Set;

public interface IConsensusEngine {

    /**
     * This method return the id of followers which response of append entry log is OK
     * */
    Set<String> calculateConsensus(String leaderId, IBrokerRaftIntegration raftIntegration);
}
