package it.polimi.ds.message.election.requests;

import java.io.Serializable;

public class RequestVote implements Serializable {

    private int term;

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
