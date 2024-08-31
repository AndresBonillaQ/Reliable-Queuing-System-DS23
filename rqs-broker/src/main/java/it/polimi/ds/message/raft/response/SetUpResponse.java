package it.polimi.ds.message.raft.response;

import it.polimi.ds.message.Response;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class SetUpResponse extends Response implements Serializable {
    public SetUpResponse(StatusEnum statusEnum, String desStatus) {
        this.status = statusEnum;
        this.desStatus = desStatus;
    }
}
