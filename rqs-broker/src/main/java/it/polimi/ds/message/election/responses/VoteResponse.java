package it.polimi.ds.message.election.responses;

import it.polimi.ds.message.Response;

import java.io.Serializable;

public class VoteResponse extends Response implements Serializable {
    private String outcome;

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VoteResponse{");
        sb.append("outcome='").append(outcome).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
