package it.polimi.ds.message.model.response;

import it.polimi.ds.message.Response;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class ReadValueResponse extends Response implements Serializable {
    private Integer value;

    public ReadValueResponse(StatusEnum statusEnum, String desEnum, Integer value) {
        this.status = statusEnum;
        this.desStatus = desEnum;
        this.value = value;
    }

    public ReadValueResponse(StatusEnum statusEnum, String desEnum) {
        this.status = statusEnum;
        this.desStatus = desEnum;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
