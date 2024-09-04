package messages.pingPong;

import java.io.Serializable;

public class PingPongMessage implements Serializable {
    private String status;

    public PingPongMessage(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
