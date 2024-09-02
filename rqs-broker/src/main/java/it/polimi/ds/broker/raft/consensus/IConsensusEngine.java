package it.polimi.ds.broker.raft.consensus;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.raft.IBrokerRaftIntegration;

import java.util.Set;

public interface IConsensusEngine {

    /**
     * This method return a Set with the id of followers which response of append entry log is OK
     * */
    Set<String> calculateConsensus(String leaderId, IBrokerRaftIntegration raftIntegration);
}
