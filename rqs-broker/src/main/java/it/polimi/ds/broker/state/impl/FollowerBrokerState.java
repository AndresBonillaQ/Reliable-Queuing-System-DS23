package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.network.follower.toLeader.FollowerToLeader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FollowerBrokerState extends BrokerState {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public FollowerBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
    }

    @Override
    public void exec() {
        executor.submit(new FollowerToLeader(brokerContext, -1));
    }

    @Override
    public void onHeartbeatTimeout(){
        this.executor.shutdown();

        brokerContext.setBrokerState(new CandidateBrokerState(brokerContext));
        brokerContext.getBrokerState().exec();
    }
}
