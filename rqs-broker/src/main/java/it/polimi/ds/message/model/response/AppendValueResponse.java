package it.polimi.ds.message.model.response;

import it.polimi.ds.message.Response;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class AppendValueResponse extends Response implements Serializable {
    public AppendValueResponse(StatusEnum statusEnum, String desStatus){
        this.status = statusEnum;
        this.desStatus = desStatus;
    }
}
