package messages.messagesToDNS;

import java.io.Serializable;

public class MessageToDNS implements Serializable {

    public MessageToDNS(String Id) {
        this.Id = Id;
    }


    private String Id;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }
}
