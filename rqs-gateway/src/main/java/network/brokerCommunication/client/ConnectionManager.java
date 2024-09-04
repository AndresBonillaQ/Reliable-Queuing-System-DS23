package network.brokerCommunication.client;
import it.polimi.ds.exception.network.ImpossibleSetUpException;
import it.polimi.ds.utils.ExecutorInstance;
import it.polimi.ds.utils.config.Timing;
import model.Gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public  class ConnectionManager implements ConnectionListener {

    public ConnectionManager() {
    }


    @Override
    public void onConnectionLost(Integer clusterID) throws IOException {

        //a task that waits the gateway to get the new leader information then starts a new connection with the
        ExecutorInstance.getInstance().getExecutorService().submit(
                () -> {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            if (Gateway.getInstance().isUpdated(clusterID)) {
                                Socket newSocket = new Socket(Gateway.getInstance().getIp(clusterID), Gateway.getInstance().getPortNumber(clusterID));
                                new CommunicationThread(newSocket, this, clusterID).start();
                                Gateway.getInstance().putOnSocketMap(clusterID, newSocket);
                                System.out.println("Connection with leader of cluster" + clusterID);
                                break;
                            } else {
                               // Gateway.getInstance().removeFromRequestMap(clusterID);
                                System.out.println("Waiting 5 seconds then try to reconnect.");
                                Thread.sleep(5000); //wait 2 sec
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
    public void startConnection(Integer clusterID) throws IOException {
        onConnectionLost(clusterID);

    }

}
