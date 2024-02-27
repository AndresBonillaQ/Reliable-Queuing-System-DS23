import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

public class DNSApp {

    public static void main(String[] args) throws IOException {
        String clusterId, ipAddress;
        int portNumber;
        Scanner in = new Scanner(System.in);
        System.out.println("Type 'stop' when you are done populating the DNS\n");
        do{
            System.out.println("Insert clusterId: ");
            clusterId = in.nextLine();
            System.out.println("Insert ip Address of the cluster: ");
            ipAddress = in.nextLine();
            DNS.getInstance().setLeaderAddress(clusterId, ipAddress);
            System.out.println("Insert the port number of the cluster: ");
            portNumber = in.nextInt();
            DNS.getInstance().setClusterPort(clusterId, portNumber);

        }while( !Objects.equals(clusterId, "stop") && !Objects.equals(ipAddress, "stop"));
        ServerDNS serverDNS = new ServerDNS();
        serverDNS.start();



    }

}