package it.polimi.ds.network2.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class LeaderWaitingForFollowersCallable implements Callable<LeaderWaitingForFollowersResponse> {

    private final String brokerId;
    private final BlockingQueue<String> responseBlockingQueue;

    public LeaderWaitingForFollowersCallable(String brokerId, BlockingQueue<String> responseBlockingQueue){
        this.brokerId = brokerId;
        this.responseBlockingQueue = responseBlockingQueue;
    }

    @Override
    public LeaderWaitingForFollowersResponse call() throws Exception {
        return new LeaderWaitingForFollowersResponse(
                this.brokerId,
                responseBlockingQueue.poll(7, TimeUnit.SECONDS)
        );
    }
}
