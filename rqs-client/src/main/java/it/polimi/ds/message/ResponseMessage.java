package it.polimi.ds.message;


import it.polimi.ds.message.response.ResponseIdEnum;

import java.io.Serializable;

public class ResponseMessage implements Serializable {
    private ResponseIdEnum id = null;
    private String content = null;
    private String clientId = null;

    public ResponseMessage(ResponseIdEnum id, String content, String clientId) {
        this.id = id;
        this.content = content;
        this.clientId = clientId;
    }

    public ResponseIdEnum getId() {
        return id;
    }

    public void setId(ResponseIdEnum id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}