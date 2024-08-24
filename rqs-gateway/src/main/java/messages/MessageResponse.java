package messages;

public class MessageResponse {
    private String id = null;
    private String content = null;
    private String clientId;

    public MessageResponse(String id, String content, String clientId) {
        this.id = id;
        this.content = content;
        this.clientId = clientId;
    }

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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}