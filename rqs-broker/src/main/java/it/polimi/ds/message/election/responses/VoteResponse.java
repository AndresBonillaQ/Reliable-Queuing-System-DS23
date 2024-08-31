package it.polimi.ds.message.election.responses;

import it.polimi.ds.message.Response;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class VoteResponse extends Response implements Serializable {
    public VoteResponse(StatusEnum statusEnum, String desStatus) {
        this.status = statusEnum;
        this.desStatus = desStatus;
    }
}
