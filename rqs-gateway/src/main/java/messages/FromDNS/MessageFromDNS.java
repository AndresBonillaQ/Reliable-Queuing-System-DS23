package messages.FromDNS;

import java.io.Serializable;
import java.util.HashMap;

public class MessageFromDNS implements Serializable {
    private String Id;
    private HashMap<String, String> content;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
    public HashMap<String, String> getContent() {
        return content;
    }



}
