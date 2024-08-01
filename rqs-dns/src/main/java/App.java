import network.BrokerHandler;
import network.ClientHandler;
import network.Server;

import java.io.IOException;

public class App {

    public static void main(String[] args) throws IOException {


        /**
         * Populating the dns
         */

       //ReadConfigFile.configDns();

        Server clientHandler = new ClientHandler(8090);
        Server brokerHandler = new BrokerHandler(8100);

        clientHandler.start();
        brokerHandler.start();




    }

}