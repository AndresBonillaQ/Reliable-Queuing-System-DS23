package it.polimi.ds.broker;

import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.broker.state.impl.FollowerBrokerState;
import it.polimi.ds.model.BrokerModel;
import it.polimi.ds.model.IBrokerModel;
import it.polimi.ds.raftLog.RaftLog;

import java.util.LinkedList;
import java.util.Queue;

public class BrokerContext {

    /**
     * State design pattern: leader/follower/candidate
     * */
    private BrokerState brokerState;

    /**
     * Contains all queues information
     * */
    private final IBrokerModel brokerModel;

    /**
     * Contains broker log
     * */
    private final Queue<RaftLog> raftLogQueue;

    /**
     * BlockingQueue used as producer - consumer for communication between threads: toGateway, toFollower,
     * each request received from toGateway will be pushed to the queue and the toFollower will poll it
     * */

    public BrokerContext(){
        this.brokerState = new FollowerBrokerState(this);
        this.brokerModel = new BrokerModel();
        this.raftLogQueue = new LinkedList<>();
    }

    public void start(){
        brokerState.exec();
    }

    public void setBrokerState(BrokerState brokerState) {
        this.brokerState = brokerState;
    }

    public BrokerState getBrokerState() {
        return brokerState;
    }

    public IBrokerModel getBrokerModel() {
        return brokerModel;
    }


}
