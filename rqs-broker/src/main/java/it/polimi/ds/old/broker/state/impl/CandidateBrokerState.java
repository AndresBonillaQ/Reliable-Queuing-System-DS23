package it.polimi.ds.old.broker.state.impl;

import it.polimi.ds.old.broker.BrokerContext;
import it.polimi.ds.old.broker.state.BrokerState;
import it.polimi.ds.old.broker.state.BrokerStateEnum;
import it.polimi.ds.old.network.leader.toFollower.LeaderToFollower;
import it.polimi.ds.old.network.leader.toGateway.LeaderToGateway;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CandidateBrokerState extends BrokerState {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    public CandidateBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        brokerStateEnum = BrokerStateEnum.CANDIDATE;
    }

    @Override
    public void exec() {
        startServer();
        startClient();
    }

    private void startServer(){
        executor.submit(() -> {
            LeaderToGateway leaderToGateway = new LeaderToGateway(brokerContext, 1, null);
            leaderToGateway.start(executor);
        });
    }

    private void startClient(){
        List.of().forEach(
                followersAddress -> executor.submit(new LeaderToFollower(brokerContext, new InetSocketAddress("", 1), null))
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
