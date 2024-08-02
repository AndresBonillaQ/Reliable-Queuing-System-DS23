package it.polimi.ds;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.utils.config.BrokerConfig;

import java.net.InetSocketAddress;
import java.util.List;

public class Leader {
    public static void main(String[] args){

        //leader open port 3000 as SERVER to brokers, 30001 as SERVERt o gateway
        BrokerContext leader = new BrokerContext(
                new BrokerConfig(
                        List.of(new InetSocketAddress("127.0.0.1", 8080)),
                        3000,
                        3001
                ),
                "1",
                "1",
                true
        );

        leader.start();
    }
}

class Follower {
    public static void main(String[] args){

        //follower open port 8080 as SERVER to brokers, 80801 as SERVER to gateway
        BrokerContext follower = new BrokerContext(
                new BrokerConfig(
                        List.of(new InetSocketAddress("127.0.0.1", 3000)),
                        8080,
                        8081
                ),
                "2",
                "1",
                false
        );

        follower.start();
    }
}
