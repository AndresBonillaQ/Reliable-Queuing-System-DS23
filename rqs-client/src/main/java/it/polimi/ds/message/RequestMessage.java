package it.polimi.ds.message;

import it.polimi.ds.message.request.RequestIdEnum;

import java.io.Serializable;

public class RequestMessage implements Serializable {
    private RequestIdEnum id = null;
    private String content = null;
    private String clientId = null;

    public RequestMessage(RequestIdEnum id, String content, String clientId) {
        this.id = id;
        this.content = content;
        this.clientId = clientId;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RequestMessage{");
        sb.append("id=").append(id);
        sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
