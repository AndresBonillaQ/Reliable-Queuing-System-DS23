package it.polimi.ds.message.raft.response;

import it.polimi.ds.message.Response;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class ServiceUnavailableResponse extends Response implements Serializable {
    public ServiceUnavailableResponse(StatusEnum statusEnum, String desStatus){
        this.status = statusEnum;
        this.desStatus = desStatus;
    }
}
