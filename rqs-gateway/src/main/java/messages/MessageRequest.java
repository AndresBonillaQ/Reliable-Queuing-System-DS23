package messages;

import java.io.Serializable;

public class MessageRequest implements Serializable {
    private String id = null;
    private String content = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
