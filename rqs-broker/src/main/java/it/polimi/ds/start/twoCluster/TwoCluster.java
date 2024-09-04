package it.polimi.ds.start.twoCluster;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.utils.config.BrokerConfig;
import it.polimi.ds.utils.config.BrokerInfo;
import it.polimi.ds.utils.config.GatewayInfo;

import java.util.List;

public class TwoCluster {
}

class Follower1 {
    public static void main(String[] args){

        //follower open port 8080 as SERVER to brokers, 8081 as SERVER to gateway
        BrokerContext leader = new BrokerContext(
                new BrokerConfig(
                        "1",
                        "0",
                        3000,
                        3001,
                        List.of(
                                new BrokerInfo("2","127.0.0.1", 8080)
                        ),
                        new GatewayInfo(
                                "127.0.0.1",
                                5001
                        ),
                        "127.0.0.1"
                ),
                false
        );

        leader.start();
    }
}

class Follower2 {
    public static void main(String[] args){

        //follower open port 8080 as SERVER to brokers, 8081 as SERVER to gateway
        BrokerContext follower = new BrokerContext(
                new BrokerConfig(
                        "2",
                        "0",
                        8080,
                        8081,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 3000)
                        ),
                        new GatewayInfo(
                                "127.0.0.1",
                                5001
                        ),
                        "127.0.0.1"
                ),
                false
        );

        follower.start();
    }
}

class Follower3 {
    public static void main(String[] args){

        //follower open port 4000 as SERVER to brokers, 4001 as SERVER to gateway
        BrokerContext follower = new BrokerContext(
                new BrokerConfig(
                        "1",
                        "1",
                        4000,
                        4001,
                        List.of(
                                new BrokerInfo("2","127.0.0.1", 8500)
                        ),
                        new GatewayInfo(
                                "127.0.0.1",
                                5001
                        ),
                        "127.0.0.1"
                ),
                false
        );

        follower.start();
    }
}

class Follower4 {
    public static void main(String[] args){

        //follower open port 4000 as SERVER to brokers, 4001 as SERVER to gateway
        BrokerContext follower = new BrokerContext(
                new BrokerConfig(
                        "2",
                        "1",
                        8500,
                        8501,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 4000)
                        ),
                        new GatewayInfo(
                                "127.0.0.1",
                                5001
                        ),
                        "127.0.0.1"
                ),
                false
        );

        follower.start();
    }
}