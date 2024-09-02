package it.polimi.ds.utils.config;


/**
 * Milliseconds
 * */
public class Timing {
    private Timing(){}

    public final static int HEARTBEAT_PERIOD_SENDING = 1500;
    public final static int HEARTBEAT_PERIOD_CHECKING_FROM = 2000;
    public final static int HEARTBEAT_PERIOD_CHECKING_FROM_TO_SUM = 15000;

    public final static int HEARTBEAT_DELAY_CHECKING = 5000;

    public final static int ELECTION_TIMEOUT = 15000;
}
