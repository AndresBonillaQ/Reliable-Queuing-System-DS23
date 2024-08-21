package it.polimi.ds.message.model.response;

import it.polimi.ds.message.Response;

import java.io.Serializable;

public class ReadValueResponse extends Response implements Serializable {
    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
