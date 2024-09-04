package it.polimi.ds.broker.state;

import it.polimi.ds.broker.BrokerContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

public abstract class BrokerState {

    protected final BrokerContext brokerContext;

    public BrokerState(BrokerContext brokerContext){
        this.brokerContext = brokerContext;
    }
    abstract public void clientToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException;
    abstract public void serverToBrokerExec(String clientBrokerId, BufferedReader in, PrintWriter out) throws IOException;
}
