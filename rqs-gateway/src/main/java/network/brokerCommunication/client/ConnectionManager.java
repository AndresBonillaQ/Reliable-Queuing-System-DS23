package network.brokerCommunication.client;
import it.polimi.ds.utils.ExecutorInstance;
import network.clientCommunication.model.Gateway;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;

public  class ConnectionManager implements ConnectionListener {

    public ConnectionManager() {
    }

    @Override
    public void onConnectionLost(String clusterID) throws IOException {

        //a task that waits the gateway to get the new leader information then starts a new connection with the
        ExecutorInstance.getInstance().getExecutorService().submit(
                () -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            if (Gateway.getInstance().isUpdated(clusterID)) {
                                Socket newSocket = new Socket(Gateway.getInstance().getIp(clusterID), Gateway.getInstance().getPortNumber(clusterID));
                                new CommunicationThread(newSocket,
                                        this, clusterID).start();
                                System.out.println("Reconnected.");
                                break;
                            } else {
                                System.out.println("Waiting 2 seconds then try to reconnect.");
                                Thread.sleep(2000); //wait 2 sec
                            }
                        } catch (IOException e) {
                            System.err.println("Reconnection failed.");

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Thread interrupted.");
                        break;
                    }
                    }
                }
        );
    }
    public void startConnection(String clusterID) throws IOException {
        onConnectionLost(clusterID);

    }

}
