package it.polimi.ds;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.message.model.request.AppendValueRequest;
import it.polimi.ds.message.model.request.CreateQueueRequest;
import it.polimi.ds.message.model.request.ReadValueRequest;
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

public class Leader {
    public static void main(String[] args){

        //leader open port 3000 as SERVER to brokers, 3001 as SERVER to gateway
        BrokerContext leader = new BrokerContext(
                new BrokerConfig(
                        "1",
                        "0",
                        4000,
                        4001,
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

        leader2.start();
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
                                ,new BrokerInfo("3","127.0.0.1", 4000)
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
                                ),
                        null,
                        "127.0.0.1"
                ),
                false
        );

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

        CreateQueueRequest createQueueRequest = new CreateQueueRequest(
                "1",
                "2"
        );

        RequestMessage requestMessage = new RequestMessage(
                RequestIdEnum.CREATE_QUEUE_REQUEST,
                GsonInstance.getInstance().getGson().toJson(createQueueRequest)
        );

        System.out.println(GsonInstance.getInstance().getGson().toJson(requestMessage));
    }
}

class Test3{
    public static void main(String[] args) {

        ReadValueRequest readValueRequest = new ReadValueRequest(
                "1",
                "2"
        );

        RequestMessage requestMessage = new RequestMessage(
                RequestIdEnum.READ_VALUE_REQUEST,
                GsonInstance.getInstance().getGson().toJson(readValueRequest)
        );

        System.out.println(GsonInstance.getInstance().getGson().toJson(requestMessage));
    }
}

class Test4{
    public static void main(String[] args) {

        AppendValueRequest appendValueRequest = new AppendValueRequest(
                "1",
                2
        );

        RequestMessage requestMessage = new RequestMessage(
                RequestIdEnum.APPEND_VALUE_REQUEST,
                GsonInstance.getInstance().getGson().toJson(appendValueRequest)
        );

        System.out.println(GsonInstance.getInstance().getGson().toJson(requestMessage));
    }
}
