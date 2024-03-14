package messages;

import messages.requests.RequestIdEnum;

import java.io.Serializable;

public class MessageRequest implements Serializable {
    private RequestIdEnum id = null;
    private String content = null;
    public MessageRequest() {}
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
