package messages.requests;

import java.io.Serializable;

public class NewIpMessage implements Serializable {

    private String clusterID;
    private String ipAddress;


    public String getClusterID() {
        return clusterID;
    }

    public void setClusterID(String clusterID) {
        this.clusterID = clusterID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
