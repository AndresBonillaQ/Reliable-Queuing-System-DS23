package it.polimi.ds.raftLog;

import it.polimi.ds.exception.raft.LogBadRequestException;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class RaftLogManager {
    private final List<RaftLog> raftLogList = new LinkedList<>();

    /**
     * @param lastLogs last logs sent by leader, the size must be lesser than raftLogList size
     * */
    public void addLog(List<RaftLog> lastLogs, RaftLog newLog) throws LogBadRequestException {
        for(int i = 0; i < lastLogs.size(); i++){
            if(!Objects.equals(lastLogs.get(lastLogs.size() - 1 - i), raftLogList.get(lastLogs.size() - 1 - i)))
                throw new LogBadRequestException();
        }

        raftLogList.add(newLog);
    }

    public void addLog(){

    }
}
