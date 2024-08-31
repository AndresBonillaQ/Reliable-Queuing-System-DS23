package it.polimi.ds.message.response;

public class ServiceUnavailableResponse extends Response{
    public ServiceUnavailableResponse(StatusEnum statusEnum, String desStatus){
        this.status = statusEnum;
        this.desStatus = desStatus;
    }
}
