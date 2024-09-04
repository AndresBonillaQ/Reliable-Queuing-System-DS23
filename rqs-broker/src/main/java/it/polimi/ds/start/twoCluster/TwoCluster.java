package it.polimi.ds.start.twoCluster;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.utils.config.BrokerConfig;
import it.polimi.ds.utils.config.BrokerInfo;
import it.polimi.ds.utils.config.GatewayInfo;

import java.util.List;

public class TwoCluster {
    protected static String CLUSTER_0 = "0";
    protected static String CLUSTER_1 = "1";
    protected static String CLUSTER_2 = "2";

    protected static String LOCALHOST = "127.0.0.1";
    protected static String MY_IP = "192.168.159.144";

    protected static String GATEWAY_IP = "192.168.159.118";
    protected static int GATEWAY_PORT = 5001;
}

class Follower1 {
    public static void main(String[] args){

        //follower open port 8080 as SERVER to brokers, 8081 as SERVER to gateway
        BrokerContext leader = new BrokerContext(
                new BrokerConfig(
                        "1",
                        TwoCluster.CLUSTER_0,
                        3000,
                        3001,
                        List.of(
                                new BrokerInfo("2",TwoCluster.LOCALHOST, 8080),
                                new BrokerInfo("3",TwoCluster.LOCALHOST, 4000)
                        ),
                        new GatewayInfo(
                                TwoCluster.GATEWAY_IP,
                                TwoCluster.GATEWAY_PORT
                        ),
                        TwoCluster.MY_IP
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
                        TwoCluster.CLUSTER_0,
                        8080,
                        8081,
                        List.of(
                                new BrokerInfo("1",TwoCluster.LOCALHOST, 3000),
                                new BrokerInfo("3",TwoCluster.LOCALHOST, 4000)
                        ),
                        new GatewayInfo(
                                TwoCluster.GATEWAY_IP,
                                TwoCluster.GATEWAY_PORT
                        ),
                        TwoCluster.MY_IP
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
                        "3",
                        TwoCluster.CLUSTER_0,
                        4000,
                        4001,
                        List.of(
                                new BrokerInfo("1",TwoCluster.LOCALHOST, 3000),
                                new BrokerInfo("2",TwoCluster.LOCALHOST, 8080)
                        ),
                        new GatewayInfo(
                                TwoCluster.GATEWAY_IP,
                                TwoCluster.GATEWAY_PORT
                        ),
                        TwoCluster.MY_IP
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
                        "1",
                        TwoCluster.CLUSTER_1,
                        3500,
                        3501,
                        List.of(
                                new BrokerInfo("2", TwoCluster.LOCALHOST, 8888),
                                new BrokerInfo("3", TwoCluster.LOCALHOST, 7500)
                        ),
                        new GatewayInfo(
                                TwoCluster.GATEWAY_IP,
                                TwoCluster.GATEWAY_PORT
                        ),
                        TwoCluster.MY_IP
                ),
                false
        );

        follower.start();
    }
}

class Follower5 {
    public static void main(String[] args){

        //follower open port 4000 as SERVER to brokers, 4001 as SERVER to gateway
        BrokerContext follower = new BrokerContext(
                new BrokerConfig(
                        "2",
                        TwoCluster.CLUSTER_1,
                        8888,
                        8889,
                        List.of(
                                new BrokerInfo("1", TwoCluster.LOCALHOST, 3500),
                                new BrokerInfo("3", TwoCluster.LOCALHOST, 7500)
                        ),
                        new GatewayInfo(
                                TwoCluster.GATEWAY_IP,
                                TwoCluster.GATEWAY_PORT
                        ),
                        TwoCluster.MY_IP
                ),
                false
        );

        follower.start();
    }
}

class Follower6 {
    public static void main(String[] args){

        //follower open port 4000 as SERVER to brokers, 4001 as SERVER to gateway
        BrokerContext follower = new BrokerContext(
                new BrokerConfig(
                        "3",
                        TwoCluster.CLUSTER_1,
                        7500,
                        7501,
                        List.of(
                                new BrokerInfo("1", TwoCluster.LOCALHOST, 3500),
                                new BrokerInfo("2", TwoCluster.LOCALHOST, 8888)
                        ),
                        new GatewayInfo(
                                TwoCluster.GATEWAY_IP,
                                TwoCluster.GATEWAY_PORT
                        ),
                        TwoCluster.MY_IP
                ),
                false
        );

        follower.start();
    }
}