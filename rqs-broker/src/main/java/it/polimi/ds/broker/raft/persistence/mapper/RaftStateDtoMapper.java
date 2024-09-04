package it.polimi.ds.broker.raft.persistence.mapper;

import it.polimi.ds.broker.raft.BrokerRaftIntegration;
import it.polimi.ds.broker.raft.IBrokerRaftIntegration;
import it.polimi.ds.broker.raft.persistence.utils.RaftStateDto;

public class RaftStateDtoMapper {

    private RaftStateDtoMapper(){}

    public static RaftStateDto mapObjectIntoDto(IBrokerRaftIntegration brokerRaftIntegration){
        return new RaftStateDto(
                brokerRaftIntegration.getRaftLogQueue(),
                brokerRaftIntegration.getCurrentTerm(),
                brokerRaftIntegration.getCurrentIndex(),
                brokerRaftIntegration.getLastCommitIndex(),
                brokerRaftIntegration.getMyBrokerId()
        );
    }

    public static BrokerRaftIntegration mapDtoIntoObject(RaftStateDto raftStateDto){
        return new BrokerRaftIntegration(
                raftStateDto.getRaftLogQueue(),
                raftStateDto.getCurrentTerm(),
                raftStateDto.getCurrentIndex(),
                raftStateDto.getLastCommitIndex(),
                raftStateDto.getMyBrokerId()
        );
    }
}
