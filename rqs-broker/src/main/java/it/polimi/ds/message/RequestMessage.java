package it.polimi.ds.message;

import it.polimi.ds.message.request.utils.RequestIdEnum;

import java.io.Serializable;

public class RequestMessage implements Serializable {
    private RequestIdEnum id = null;
    private String content = null;

    public RequestMessage(RequestIdEnum id, String content) {
        this.id = id;
        this.content = content;
    }

    public RequestIdEnum getId() {
        return id;
    }

    public void setId(RequestIdEnum id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
