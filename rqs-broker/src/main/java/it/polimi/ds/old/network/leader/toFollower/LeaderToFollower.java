package it.polimi.ds.old.network.leader.toFollower;

import it.polimi.ds.old.broker.BrokerContext;
import it.polimi.ds.utils.GsonInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class LeaderToFollower implements Runnable{

    private final InetSocketAddress followerAddress;
    private final Logger log = Logger.getLogger(LeaderToFollower.class.getName());
    private final BlockingQueue<String> requestBlockingQueue;
    private final BrokerContext brokerContext;

    public LeaderToFollower(BrokerContext brokerContext, InetSocketAddress followerAddress, BlockingQueue<String> requestBlockingQueue){
        this.followerAddress = followerAddress;
        this.requestBlockingQueue = requestBlockingQueue;
        this.brokerContext = brokerContext;
    }

    /**
     * Leader connect to each follower actuating as client
     * */
    @Override
    public void run() {
        try(
                Socket socket = new Socket(followerAddress.getHostName(), followerAddress.getPort());
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                BufferedReader in = new BufferedReader(streamReader);
        ){
            String requestToForward;
            String response;
            //socket.setSoTimeout(gatewayConfig.getTimeout());

            while(true){

                try{
                    requestToForward = requestBlockingQueue.take();
                } catch (InterruptedException e) {
                    log.severe("Interrupted exception throw by put in blockingQueue, closing gateway connection!");
                    return;
                }

                //Forward request to follower
                writer.println(GsonInstance.getInstance().getGson().toJson(requestToForward));
                writer.flush();

                try{
                    //Receive response from follower
                    response = in.readLine();
                }catch (IOException e){
                    log.severe("Io exception reading from gateway socket!");
                    return;
                }

            }

        } catch (IOException ex) {
            log.severe("Error! IOException");
            log.severe(ex.getMessage());
        }
    }
}
