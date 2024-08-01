package network.clientCommunication.model.initialization;
import model.Dns;
import network.clientCommunication.model.Gateway;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadConfigFile {
    public static void initialization() {
        String filePath = "dnsconfig.txt";
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;

            while ((line = reader.readLine()) != null) {
                String[] wholeLine = line.split(" ");
                Gateway.getInstance().setIp(wholeLine[0], wholeLine[1]);
                Gateway.getInstance().setPortNumber(Integer.parseInt(wholeLine[2]) , wholeLine[0]);
                Gateway.getInstance().fillHashMaps(wholeLine[0], wholeLine[1], Integer.parseInt(wholeLine[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}