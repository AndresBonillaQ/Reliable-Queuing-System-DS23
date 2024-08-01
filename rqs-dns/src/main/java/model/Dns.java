package model;

import java.util.HashMap;

public class Dns {
    private static Dns instance = null;
    public static Dns getInstance() {
        if (instance == null) {
            instance = new Dns();
        }
        return instance;
    }
    private final HashMap<String, String> addressMap = new HashMap<String, String>(); //<clusterId, ipAddress>
    private final HashMap<String, Integer> portMap = new HashMap<String, Integer>(); //<clusterId, portNumber>
    public Dns() {
    }
    public void setLeaderAddress(String clusterId, String ipAddress) {
        addressMap.put(clusterId,ipAddress );
    }
    public String getAddress(String clusterId) {
        return addressMap.get(clusterId);
    }
    public void setClusterPort(String clusterId, int portNumber) {
        portMap.put(clusterId, portNumber);
    }
    public int getPort(String clusterId) {
        return portMap.get(clusterId);
    }

}