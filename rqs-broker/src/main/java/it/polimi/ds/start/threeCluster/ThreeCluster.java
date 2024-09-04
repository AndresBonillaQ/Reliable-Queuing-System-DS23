package it.polimi.ds.start.threeCluster;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.utils.config.BrokerConfig;
import it.polimi.ds.utils.config.BrokerInfo;
import it.polimi.ds.utils.config.GatewayInfo;

import java.util.List;

public class ThreeCluster {
    protected static String CLUSTER_0 = "0";
    protected static String CLUSTER_1 = "1";
    protected static String CLUSTER_2 = "2";

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
                        ThreeCluster.CLUSTER_0,
                        3000,
                        3001,
                        List.of(
                                new BrokerInfo("2","127.0.0.1", 8080)
                        ),
                        new GatewayInfo(
                                ThreeCluster.GATEWAY_IP,
                                ThreeCluster.GATEWAY_PORT
                        ),
                        ThreeCluster.MY_IP
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
                        ThreeCluster.CLUSTER_0,
                        8080,
                        8081,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 3000)
                        ),
                        new GatewayInfo(
                                ThreeCluster.GATEWAY_IP,
                                5001
                        ),
                        ThreeCluster.MY_IP
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
                        ThreeCluster.CLUSTER_1,
                        4000,
                        4001,
                        List.of(
                                new BrokerInfo("2","127.0.0.1", 8500)
                        ),
                        new GatewayInfo(
                                ThreeCluster.GATEWAY_IP,
                                5001
                        ),
                        ThreeCluster.MY_IP
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
                        ThreeCluster.CLUSTER_1,
                        8500,
                        8501,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 4000)
                        ),
                        new GatewayInfo(
                                ThreeCluster.GATEWAY_IP,
                                5001
                        ),
                        ThreeCluster.MY_IP
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
                        "1",
                        ThreeCluster.CLUSTER_2,
                        8888,
                        8889,
                        List.of(
                                new BrokerInfo("2","127.0.0.1", 4200)
                        ),
                        new GatewayInfo(
                                "192.168.159.118",
                                5001
                        ),
                        ThreeCluster.MY_IP
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
                        "2",
                        ThreeCluster.CLUSTER_2,
                        4200,
                        4201,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 8888)
                        ),
                        new GatewayInfo(
                                ThreeCluster.GATEWAY_IP,
                                ThreeCluster.GATEWAY_PORT
                        ),
                        ThreeCluster.MY_IP
                ),
                false
        );

        follower.start();
    }
}