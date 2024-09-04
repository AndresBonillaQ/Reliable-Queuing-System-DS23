package it.polimi.ds;

import it.polimi.ds.utils.config.GatewayConfig;

import java.io.IOException;

public class ClientApp {
    public static void main(String[] args) throws IOException {

        Client client = new Client(
                new GatewayConfig(
                        "127.0.0.1",
                        6666,
                        0)
        );

        client.start();
    }
}
