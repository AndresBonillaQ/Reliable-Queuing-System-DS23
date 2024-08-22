package it.polimi.ds.broker;

import it.polimi.ds.broker.model.IBrokerModel;
import it.polimi.ds.broker.model.impl.BrokerModel;
import it.polimi.ds.broker.raft.IBrokerRaftIntegration;
import it.polimi.ds.broker.raft.impl.BrokerRaftIntegration;
import it.polimi.ds.broker.state.BrokerState;
import it.polimi.ds.broker.state.impl.FollowerBrokerState;
import it.polimi.ds.broker.state.impl.LeaderBrokerState;
import it.polimi.ds.network.broker.client.ClientToBroker;
import it.polimi.ds.network.broker.server.ServerToBroker;
import it.polimi.ds.network.gateway.client.ClientToGateway;
import it.polimi.ds.network.gateway.server.ServerToGateway;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.config.BrokerConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BrokerContext {

    /**
     * The cluster leader id, useful to followers
     * */
    private String leaderId;

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
    private final BrokerConfig myBrokerConfig;

    private final Logger log = Logger.getLogger(BrokerContext.class.getName());

    public BrokerContext(BrokerConfig myBrokerConfig, boolean isLeader, String brokerIdLeader, boolean canBecomeCandidate){
        this.numClusterBrokers = myBrokerConfig.getClusterBrokerConfig().size() + 1;

        this.myBrokerConfig = myBrokerConfig;

        if(isLeader){
            leaderId = myBrokerConfig.getMyBrokerId();
            brokerState = new LeaderBrokerState(this);
        }
        else{
            brokerState = new FollowerBrokerState(this);
            leaderId = brokerIdLeader;
        }
    }

    /**
     * Start the broker instance
     * */
    public void start(){
        log.log(Level.INFO, "Starting broker with ID {0}", myBrokerConfig.getMyBrokerId());

        ExecutorInstance.getInstance().getExecutorService().submit(new ServerToGateway(this, myBrokerConfig.getBrokerServerPortToGateway()));
        ExecutorInstance.getInstance().getExecutorService().submit(new ServerToBroker(this, myBrokerConfig.getBrokerServerPortToBrokers()));
        //ExecutorInstance.getInstance().getExecutorService().submit(new ClientToGateway(this, myBrokerConfig.getGatewayInfo()));
        myBrokerConfig.getClusterBrokerConfig().forEach(brokerInfo -> ExecutorInstance.getInstance().getExecutorService().submit(new ClientToBroker(brokerInfo, this)));
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

    public IBrokerRaftIntegration getBrokerRaftIntegration() {
        return brokerRaftIntegration;
    }

    public int getNumClusterBrokers() {
        return numClusterBrokers;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public BrokerConfig getMyBrokerConfig() {
        return myBrokerConfig;
    }

    public void updateNewLeaderInfo(String leaderId){
        this.leaderId = leaderId;
    }
}
