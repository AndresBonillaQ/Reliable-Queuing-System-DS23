package it.polimi.ds.message.election.responses;

import java.io.Serializable;

public class VoteResponse implements Serializable {
    private String outcome;

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }
}
