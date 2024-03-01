import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class DNSApp {

    public static void main(String[] args) throws IOException {
        String clusterId, ipAddress;
        int portNumber;

        /**
         * Populating the dns
         */
        Scanner in = new Scanner(System.in);
        do{
            System.out.println("Insert clusterId: ");
            clusterId = in.nextLine();
            System.out.println("Insert ip Address of the cluster: ");
            ipAddress = in.nextLine();
            DNS.getInstance().setLeaderAddress(clusterId, ipAddress);
            System.out.println("Insert the port number of the cluster: ");
            portNumber = Integer.parseInt(in.nextLine());
            DNS.getInstance().setClusterPort(clusterId, portNumber);
            System.out.println("Insert other clusters?(Y/N):  ");
        }while ( !in.nextLine().equals("N") );

        ServerDNS serverDNS = new ServerDNS();
        serverDNS.start();



    }

}