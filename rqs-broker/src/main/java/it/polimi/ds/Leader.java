package it.polimi.ds;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.utils.BrokerInfo;
import it.polimi.ds.utils.config.BrokerConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Leader {
    public static void main(String[] args){

        //leader open port 3000 as SERVER to brokers, 3001 as SERVER to gateway
        BrokerContext leader = new BrokerContext(
                new BrokerConfig(
                        "1",
                        "1",
                        3000,
                        3001,
                        List.of(
                                new BrokerInfo("2","127.0.0.1", 8080)
                                //,new BrokerInfo("3","127.0.0.1", 4000)
                        )
                ),
                true
        );

        leader.start();
    }
}

class Follower {
    public static void main(String[] args){

        //follower open port 8080 as SERVER to brokers, 8081 as SERVER to gateway
        BrokerContext follower = new BrokerContext(
                new BrokerConfig(
                        "2",
                        "1",
                        8080,
                        8081,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 3000)
                                //,new BrokerInfo("3","127.0.0.1", 4000)
                        )
                ),
                false);

        follower.start();
    }
}

class Follower2 {
    public static void main(String[] args){

        //follower open port 4000 as SERVER to brokers, 4001 as SERVER to gateway
        BrokerContext follower = new BrokerContext(
                new BrokerConfig(
                        "3",
                        "1",
                        4000,
                        4001,
                        List.of(
                                new BrokerInfo("1","127.0.0.1", 3000),
                                new BrokerInfo("2","127.0.0.1", 8080)
                                )
                ),
                false);

        follower.start();
    }
}

class Test{
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(4000);

        Socket socket = serverSocket.accept();

        InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
        BufferedReader in = new BufferedReader(inputStreamReader);
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        while(true){
            String msg = in.readLine();
            out.println(msg);
            out.flush();
        }
    }
}

class Test2{
    public static void main(String[] args) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("127.0.0.1", 3000);

        System.out.println("Hostname : " + inetSocketAddress.getHostString());
        System.out.println("Port :" + inetSocketAddress.getPort());
    }
}
