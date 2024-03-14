package utils;

import utils.ResponseIdEnum;

import java.io.Serializable;

public class ResponseMessage implements Serializable {
    private ResponseIdEnum id = null;
    private String content = null;

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
}
