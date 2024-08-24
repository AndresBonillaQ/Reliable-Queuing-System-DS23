package it.polimi.ds.utils.config;


/**
 * Milliseconds
 * */
public class Timing {
    private Timing(){}

    public final static int HEARTBEAT_PERIOD_SENDING = 2000;
    public final static int HEARTBEAT_PERIOD_CHECKING_FROM = 2500;
    public final static int HEARTBEAT_PERIOD_CHECKING_FROM_TO_SUM = 9000;

    public final static int HEARTBEAT_DELAY_SENDING = 5000;
    public final static int HEARTBEAT_DELAY_CHECKING = 15000;
}
