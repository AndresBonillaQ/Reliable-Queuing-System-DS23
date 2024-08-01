package it.polimi.ds.network2.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LeaderWaitingForFollowersCallable implements Callable<String> {

    private final BlockingQueue<String> responseBlockingQueue;
    private final Logger log = Logger.getLogger(LeaderWaitingForFollowersCallable.class.getName());

    public LeaderWaitingForFollowersCallable(BlockingQueue<String> responseBlockingQueue){
        this.responseBlockingQueue = responseBlockingQueue;
    }

    @Override
    public String call() throws Exception {
        log.log(Level.INFO, "ResponseBlockingQueue of callable is: {0}", responseBlockingQueue);
        return responseBlockingQueue.poll(15, TimeUnit.SECONDS);
    }
}
