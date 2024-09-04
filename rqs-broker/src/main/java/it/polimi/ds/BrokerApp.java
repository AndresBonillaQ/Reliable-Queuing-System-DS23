package it.polimi.ds;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.config.BrokerConfig;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class BrokerApp {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java App <config-file-path>");
            return;
        }

        String configFilePath = args[0];

        Optional<BrokerConfig> brokerConfig = loadFromConfig(configFilePath);

        if(brokerConfig.isPresent()){
            BrokerContext follower = new BrokerContext(brokerConfig.get(), false);
            follower.start();
        } else
            System.out.println("Killing broker..");
    }

    private static Optional<BrokerConfig> loadFromConfig(String configPath){
        try (Reader reader = new FileReader(configPath)) {
            BrokerConfig brokerConfig = GsonInstance.getInstance().getGson().fromJson(reader, BrokerConfig.class);
            return Optional.of(brokerConfig);
        } catch (FileNotFoundException e){
            System.out.println("Config File not found, impossible to run!");
            return Optional.empty();
        } catch (IOException e) {
            System.out.println("IO ERROR during reading config file, impossible to run! {0}");
            return Optional.empty();
        }
    }
}
