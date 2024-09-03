package it.polimi.ds.utils.config;


/**
 * Milliseconds
 * */
public class Timing {
    private Timing(){}

    public final static int HEARTBEAT_PERIOD_SENDING = 1500;
    public final static int HEARTBEAT_PERIOD_CHECKING_OFFSET = 4000;
    public final static int HEARTBEAT_PERIOD_CHECKING_WINDOW = 6000;

    // These variables will be multiplied with numOfBrokersInCluster
    public final static int HEARTBEAT_FIRST_SET_UP_NEEDED = 3500;
    public final static int ELECTION_TIMEOUT = 1000;
}
