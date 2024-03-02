package it.polimi.ds.broker.state.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.network.leader.toFollower.LeaderToFollower;
import it.polimi.ds.network.leader.toGateway.LeaderToGateway;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CandidateBrokerState extends BrokerState {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public CandidateBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
    }

    @Override
    public void exec() {
        startServer();
        startClient();
    }

    private void startServer(){
        executor.submit(() -> {
            LeaderToGateway leaderToGateway = new LeaderToGateway(brokerContext, 1, requestBlockingQueue);
            leaderToGateway.start(executor);
        });
    }

    private void startClient(){
        List.of().forEach(
                followersAddress -> executor.submit(new LeaderToFollower(brokerContext, new InetSocketAddress("", 1), requestBlockingQueue))
        );
    }

    @Override
    public void onWinLeaderElection() {
        executor.shutdown();

        brokerContext.setBrokerState(new LeaderBrokerState(brokerContext));
        brokerContext.getBrokerState().exec();
    }

    @Override
    public void onLoseLeaderElection() {
        executor.shutdown();

        brokerContext.setBrokerState(new FollowerBrokerState(brokerContext));
        brokerContext.getBrokerState().exec();
    }
}
