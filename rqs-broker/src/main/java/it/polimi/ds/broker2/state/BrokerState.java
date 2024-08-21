package it.polimi.ds.broker2.state;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.message.RequestMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class BrokerState {

    protected final BrokerContext brokerContext;

    protected final Object statusLock = new Object();

    public BrokerState(BrokerContext brokerContext){
        this.brokerContext = brokerContext;
    }

    abstract public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException, InterruptedException;
    abstract public void clientToGatewayExec(BufferedReader in, PrintWriter out);
    abstract public void serverToGatewayExec(BufferedReader in, PrintWriter out) throws IOException;
    abstract public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException;

    public void onHeartbeatTimeout(){}

    public void onWinLeaderElection(PrintWriter out){}

    public void onLoseLeaderElection(){}

    public void onDiscoverLeaderWithHigherTerm(){} //?

    public BrokerContext getBrokerContext() {
        return brokerContext;
    }

    public Object getStatusLock() {
        return statusLock;
    }



}
