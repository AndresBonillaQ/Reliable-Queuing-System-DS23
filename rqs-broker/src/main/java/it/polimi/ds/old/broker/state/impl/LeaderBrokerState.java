package it.polimi.ds.old.broker.state.impl;

import it.polimi.ds.old.broker.BrokerContext;
import it.polimi.ds.old.broker.state.BrokerState;
import it.polimi.ds.old.broker.state.BrokerStateEnum;
import it.polimi.ds.old.network.leader.toFollower.LeaderToFollower;
import it.polimi.ds.old.network.leader.toGateway.LeaderToGateway;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class LeaderBrokerState extends BrokerState {

    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * BlockingQueue used as producer - consumer for communication between threads: toGateway, toFollower,
     * each request received from toGateway will be pushed to the queue and the toFollower will poll it
     * */
    private final BlockingQueue<String> requestBlockingQueue = new LinkedBlockingQueue<>();

    public LeaderBrokerState(BrokerContext brokerContext) {
        super(brokerContext);
        brokerStateEnum = BrokerStateEnum.LEADER;
    }

    @Override
    public void exec() {
        startServer();
        startClient();
    }

    /**
     * Leader act as server from point of view of Gateway
     * */
    private void startServer(){
        executor.submit(() -> {
            LeaderToGateway leaderToGateway = new LeaderToGateway(brokerContext, 1, requestBlockingQueue);
            leaderToGateway.start(executor);
        });
    }

    /**
     * Leader act as client from point of view of follower
     * */
    private void startClient(){
        List.of().forEach(
                followersAddress -> executor.submit(new LeaderToFollower(brokerContext, new InetSocketAddress("", 1), requestBlockingQueue))
        );
    }

    //da vedere se c'è bisogno
    public void onDiscoverLeaderWithHigherTerm(){
        executor.shutdown();

        brokerContext.setBrokerState(new FollowerBrokerState(brokerContext));
        brokerContext.getBrokerState().exec();
    }
}
