package it.polimi.ds;

import it.polimi.ds.utils.config.GatewayConfig;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {

        GatewayConfig gatewayConfig = new GatewayConfig();
        gatewayConfig.setIp("192.168.107.118");
        gatewayConfig.setPort(8081);
        gatewayConfig.setTimeout(100);

        Client client = new Client(gatewayConfig, "1");
        client.start();
    }
}
