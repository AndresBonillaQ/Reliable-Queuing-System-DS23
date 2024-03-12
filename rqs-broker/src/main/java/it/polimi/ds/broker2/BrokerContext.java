package it.polimi.ds.broker2;

import it.polimi.ds.broker2.state.BrokerState;
import it.polimi.ds.broker2.state.impl.FollowerBrokerState;
import it.polimi.ds.broker2.state.impl.LeaderBrokerState;
import it.polimi.ds.model.BrokerModel;
import it.polimi.ds.model.IBrokerModel;
import it.polimi.ds.network2.broker.client.ClientToBroker;
import it.polimi.ds.network2.broker.server.ServerToBroker;
import it.polimi.ds.network2.gateway.ServerToGateway;
import it.polimi.ds.raftLog.RaftLog;
import it.polimi.ds.utils.ExecutorInstance;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class BrokerContext {

    /**
     * The broker id
     * */
    private final String brokerId;

    /**
     * Number of brokers which compose the cluster
     * */
    private final int numBrokersInCluster;

    /**
     * State design pattern: leader/follower/candidate
     * */
    private BrokerState brokerState; //= new FollowerBrokerState(this);

    /**
     * Contains all queues information
     * */
    private final IBrokerModel brokerModel = new BrokerModel();

    /**
     * Contains broker log
     * */
    private final Queue<RaftLog> raftLogQueue = new LinkedList<>();;

    /**
     * Number of brokers which compose the cluster
     * */
    private final List<InetSocketAddress> clusterBrokerAddress;

    private final int brokerServerPort;

    public BrokerContext(int brokerServerPort, List<InetSocketAddress> clusterBrokerAddress, String brokerId, boolean isLeader){
        this.clusterBrokerAddress = clusterBrokerAddress;
        this.numBrokersInCluster = clusterBrokerAddress.size() + 1;
        this.brokerId = brokerId;
        this.brokerServerPort = brokerServerPort;

        if(isLeader)
            brokerState = new LeaderBrokerState(this);
        else
            brokerState = new FollowerBrokerState(this);
    }

    public void start(){
        //ExecutorInstance.getInstance().getExecutorService().submit(new ServerToGateway(this, 8081));
        ExecutorInstance.getInstance().getExecutorService().submit(new ServerToBroker(this, brokerServerPort));
        clusterBrokerAddress.forEach(address -> ExecutorInstance.getInstance().getExecutorService().submit(new ClientToBroker( address, this)));
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
