package it.polimi.ds.message.response;

import it.polimi.ds.message.response.utils.StatusEnum;

public abstract class Response {
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
