package it.polimi.ds;

import it.polimi.ds.broker2.BrokerContext;

import java.net.InetSocketAddress;
import java.util.List;

public class Leader {
    public static void main(String[] args){
        BrokerContext leader = new BrokerContext(
                3000,
                List.of(new InetSocketAddress("127.0.0.1", 3001)),
                "1",
                true
        );

        leader.start();
    }
}

class Client {
    public static void main(String[] args){

        BrokerContext follower = new BrokerContext(
                3001,
                List.of(new InetSocketAddress("127.0.0.1", 3000)),
                "1",
                false
        );

        follower.start();
    }
}
