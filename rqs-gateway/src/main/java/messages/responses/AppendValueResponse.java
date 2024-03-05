package messages.responses;

import java.io.Serializable;


public class AppendValueResponse implements Serializable {
    protected StatusEnum status;
    protected String desStatus;

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getDesStatus() {
        return desStatus;
    }

    public void setDesStatus(String desStatus) {
        this.desStatus = desStatus;
    }
}
