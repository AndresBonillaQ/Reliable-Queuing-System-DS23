package it.polimi.ds.broker.raft.utils;

import java.io.Serializable;

public class RaftLog implements Serializable {
    private Integer term;
    private String request;
    private boolean committed;  // maybe unused

    public RaftLog(){}

    public RaftLog(Integer term, String request) {
        this.term = term;
        this.request = request;
        this.committed = false;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(Integer term) {
        this.term = term;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RaftLog{");
        sb.append("term=").append(term);
        sb.append(", request='").append(request).append('\'');
        sb.append(", committed=").append(committed);
        sb.append('}');
        return sb.toString();
    }
}
