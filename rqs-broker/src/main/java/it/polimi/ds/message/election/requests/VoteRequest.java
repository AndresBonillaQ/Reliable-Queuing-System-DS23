package it.polimi.ds.message.election.requests;

import it.polimi.ds.message.Response;

import java.io.Serializable;

public class VoteRequest implements Serializable {

    private int term;

    public VoteRequest(int term) {
        this.term = term;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestVote{");
        sb.append("term=").append(term);
        sb.append('}');
        return sb.toString();
    }
}
