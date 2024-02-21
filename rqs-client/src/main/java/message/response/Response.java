package message.response;

public abstract class Response {
    protected String status;
    protected String desStatus;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDesStatus() {
        return desStatus;
    }

    public void setDesStatus(String desStatus) {
        this.desStatus = desStatus;
    }
}
