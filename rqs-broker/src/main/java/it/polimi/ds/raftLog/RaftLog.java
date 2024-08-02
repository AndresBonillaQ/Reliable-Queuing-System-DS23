package it.polimi.ds.raftLog;

import java.io.Serializable;

public class RaftLog implements Serializable {
    private Integer term;
    private Integer index;
    private String request;
    private boolean committed;

    public RaftLog(){}

    public RaftLog(Integer term, Integer index, String request) {
        this.term = term;
        this.index = index;
        this.request = request;
        this.committed = false;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public boolean isCommitted() {
        return committed;
    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }
}
