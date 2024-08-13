package it.polimi.ds.broker2;

import it.polimi.ds.broker2.model.IBrokerModel;
import it.polimi.ds.broker2.model.impl.BrokerModel;
import it.polimi.ds.broker2.raft.IBrokerRaftIntegration;
import it.polimi.ds.broker2.raft.impl.BrokerRaftIntegration;
import it.polimi.ds.broker2.state.BrokerState;
import it.polimi.ds.broker2.state.impl.FollowerBrokerState;
import it.polimi.ds.broker2.state.impl.LeaderBrokerState;
import it.polimi.ds.network2.broker.client.ClientToBroker;
import it.polimi.ds.network2.broker.server.ServerToBroker;
import it.polimi.ds.network2.gateway.ServerToGateway;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.ThreadsHealth;
import it.polimi.ds.utils.config.BrokerConfig;

import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrokerContext {

    /**
     * The broker id
     * */
    private final String brokerId;

    /**
     * The cluster leader id, useful to followers
     * */
    private String leaderId;

    /**
     * The cluster leader socket, useful to followers
     * */
    private Socket leaderSocket;

    /**
     * The cluster id
     * */
    private final String clusterId;

    /**
     * The numClusterBrokers
     * */
    private final int numClusterBrokers;

    /**
     * State design pattern: leader/follower/candidate
     * */
    private BrokerState brokerState; //= new FollowerBrokerState(this);

    /**
     * The object which contains all queues information
     * */
    private final IBrokerModel brokerModel = new BrokerModel();

    /**
     * The object which contains all information about raft integration
     * */
    private final IBrokerRaftIntegration brokerRaftIntegration = new BrokerRaftIntegration();

    /**
     * Broker network configuration
     * */
    private final BrokerConfig brokerConfig;

    private final Logger log = Logger.getLogger(BrokerContext.class.getName());

    public BrokerContext(BrokerConfig brokerConfig, String brokerId, String clusterId, boolean isLeader){
        this.brokerId = brokerId;
        this.clusterId = clusterId;
        this.numClusterBrokers = brokerConfig.getClusterBrokerConfig().size() + 1;

        this.brokerConfig = brokerConfig;

        if(isLeader)
            brokerState = new LeaderBrokerState(this);
        else
            brokerState = new FollowerBrokerState(this);
    }

    /**
     * Start the broker instance
     * */
    public void start(){
        log.log(Level.INFO, "Starting broker with ID {0}", brokerId);
        new Thread(new ThreadsHealth()).start();
        ExecutorInstance.getInstance().getExecutorService().submit(new ServerToGateway(this, brokerConfig.getBrokerServerPortToGateway()));
        ExecutorInstance.getInstance().getExecutorService().submit(new ServerToBroker(this, brokerConfig.getBrokerServerPortToBrokers()));
        brokerConfig.getClusterBrokerConfig().forEach(brokerInfo -> ExecutorInstance.getInstance().getExecutorService().submit(new ClientToBroker(brokerInfo.getInetSocketAddress(), this, brokerInfo.getBrokerId())));
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

    public String getBrokerId() {
        return brokerId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public IBrokerRaftIntegration getBrokerRaftIntegration() {
        return brokerRaftIntegration;
    }

    public int getNumClusterBrokers() {
        return numClusterBrokers;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public Socket getLeaderSocket() {
        return leaderSocket;
    }

    public void updateNewLeaderInfo(String leaderId, Socket leaderSocket){
        this.leaderId = leaderId;
        this.leaderSocket = leaderSocket;
    }
}
