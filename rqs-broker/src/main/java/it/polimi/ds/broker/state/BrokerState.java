package it.polimi.ds.broker.state;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.exception.network.ImpossibleSetUpException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.model.response.utils.StatusEnum;
import it.polimi.ds.message.raft.response.SetUpResponse;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.NetworkMessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public abstract class BrokerState {

    protected final BrokerContext brokerContext;

    protected final Object isReadyLock = new Object();

    public BrokerState(BrokerContext brokerContext){
        this.brokerContext = brokerContext;
    }

    abstract public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException;
    abstract public void clientToGatewayExec(BufferedReader in, PrintWriter out) throws IOException;
    abstract public void serverToGatewayExec(BufferedReader in, PrintWriter out) throws IOException;
    abstract public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException;

    public void onHeartbeatTimeout(){}

    public void onWinLeaderElection(PrintWriter out){}

    public void onLoseLeaderElection(){}

    public void onDiscoverLeaderWithHigherTerm(){} //?
}
