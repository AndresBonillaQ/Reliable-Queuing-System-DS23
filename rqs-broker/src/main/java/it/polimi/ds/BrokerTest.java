package it.polimi.ds;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.message.model.request.AppendValueRequest;
import it.polimi.ds.utils.config.BrokerInfo;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.config.BrokerConfig;
import it.polimi.ds.utils.config.GatewayInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class BrokerTest {
    public static void main(String[] args){

        //leader open port 3000 as SERVER to brokers, 3001 as SERVER to gateway
        BrokerContext leader = new BrokerContext(
                new BrokerConfig(
                        "1",
                        "0",
                        3000,
                        3001,
                        List.of(
                                new BrokerInfo("2","127.0.0.1", 8080),
                                new BrokerInfo("3","127.0.0.1", 4000),
                                new BrokerInfo("4","127.0.0.1", 6000)
                        ),
                        new GatewayInfo(
                                "127.0.1",
                                5001
                        ),
                        "127.0.0.1"
                ),
                false
        );

        leader.start();
    }
}

class Leader1 {
    public static void main(String[] args){

        //leader open port 4200 as SERVER to brokers, 4002 as SERVER to gateway
        BrokerContext leader = new BrokerContext(
                new BrokerConfig(
                        "1",
                        "1",
                        4200,
                        4002,
                        List.of(
                                //new BrokerInfo("2","127.0.0.1", 8080)
                                //,new BrokerInfo("3","127.0.0.1", 4000)
                        ),
                        new GatewayInfo(
                                "127.0.1",
                                5001
                        ),
                        "127.0.0.1"
                ),
                true
        );

        leader.start();
    }
}

class Leader2 {
    public static void main(String[] args){

        //leader open port 3000 as SERVER to brokers, 3001 as SERVER to gateway
        BrokerContext leader2 = new BrokerContext(
                new BrokerConfig(
                        "2",
                        "2",
                        3000,
                        3001,
                        List.of(
                                new BrokerInfo("2","127.0.0.1", 8080)
                                ,new BrokerInfo("3","127.0.0.1", 4000)
                        ),
                        new GatewayInfo(
                                "127.0.1",
                                5001
                        ),
                        "127.0.0.1"
                ),
                true
        );

        leader2.start();
    }
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
                                new BrokerInfo("2","127.0.0.1", 8080),
                                new BrokerInfo("3","127.0.0.1", 4000),
                                new BrokerInfo("4","127.0.0.1", 6000)
                        ),
                        new GatewayInfo(
                                "127.0.1",
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
                                new BrokerInfo("1","127.0.0.1", 3000),
                                new BrokerInfo("3","127.0.0.1", 4000),
                                new BrokerInfo("4","127.0.0.1", 6000)
                        ),
                        new GatewayInfo(
                                "127.0.1",
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
                        "3",
                        "0",
                        4000,
                        4001,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 3000),
                                new BrokerInfo("2","127.0.0.1", 8080),
                                new BrokerInfo("4","127.0.0.1", 6000)
                                ),
                        new GatewayInfo(
                                "127.0.1",
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
                        "4",
                        "0",
                        6000,
                        6001,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 3000),
                                new BrokerInfo("2","127.0.0.1", 8080),
                                new BrokerInfo("3","127.0.0.1", 4000)
                        ),
                        new GatewayInfo(
                                "127.0.1",
                                5001
                        ),
                        "127.0.0.1"
                ),
                false
        );

        follower.start();
    }
}