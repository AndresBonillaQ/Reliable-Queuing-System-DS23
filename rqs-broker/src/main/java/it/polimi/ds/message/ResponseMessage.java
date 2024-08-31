package it.polimi.ds.message;

import it.polimi.ds.message.id.ResponseIdEnum;
import it.polimi.ds.message.model.response.utils.StatusEnum;

import java.io.Serializable;

public class ResponseMessage implements Serializable {
    private ResponseIdEnum id = null;
    private String content = null;
    private String clientId = null;

    public ResponseMessage(ResponseIdEnum id, String content) {
        this.id = id;
        this.content = content;
    }

    public ResponseMessage(ResponseIdEnum id, String content, String clientId) {
        this.clientId = clientId;
        this.id = id;
        this.content = content;
    }

    public ResponseMessage(){}

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ResponseMessage{");
        sb.append("id=").append(id);
        sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
