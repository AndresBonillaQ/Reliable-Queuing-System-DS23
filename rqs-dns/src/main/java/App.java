import java.io.IOException;
import java.util.Scanner;

public class App {

    public static void main(String[] args) throws IOException {
        String clusterId, ipAddress;
        int portNumber;

        /**
         * Populating the dns, for testing
         */
        Scanner in = new Scanner(System.in);
        do{
            System.out.println("Insert clusterId: ");
            clusterId = in.nextLine();
            System.out.println("Insert ip Address of the cluster: ");
            ipAddress = in.nextLine();
            Dns.getInstance().setLeaderAddress(clusterId, ipAddress);
            System.out.println("Insert the port number of the cluster: ");
            portNumber = Integer.parseInt(in.nextLine());
            Dns.getInstance().setClusterPort(clusterId, portNumber);
            System.out.println("Insert other clusters?(Y/N):  ");
        }while ( !in.nextLine().equals("N") );

        Server clientHandler = new ClientHandler(8090);
        Server brokerHandler = new BrokerHandler(8100);

        clientHandler.start();
        brokerHandler.start();




    }

}