package it.polimi.ds.exception.raft;

public class LogBadRequestException extends Exception{
    public LogBadRequestException(String msg){
        super(msg);
    }
}
