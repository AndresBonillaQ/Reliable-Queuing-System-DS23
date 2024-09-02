package it.polimi.ds;

import it.polimi.ds.utils.config.GatewayConfig;

import java.io.IOException;

public class ClientApp {
    public static void main(String[] args) throws IOException {

        GatewayConfig gatewayConfig = new GatewayConfig();
        gatewayConfig.setIp("127.0.0.1");
        gatewayConfig.setPort(6666);
        gatewayConfig.setTimeout(0);

        Client client = new Client(gatewayConfig);
        client.start();
    }
}
