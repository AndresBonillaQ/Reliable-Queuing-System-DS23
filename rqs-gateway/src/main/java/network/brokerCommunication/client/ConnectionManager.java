package network.brokerCommunication.client;

import model.Gateway;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

public  class ConnectionManager implements ConnectionListener {

    public ConnectionManager() {
    }


    @Override
    public void onConnectionLost(Integer clusterID) throws IOException {

        //a task that waits the gateway to get the new leader information then starts a new connection with the
        Executors.newSingleThreadExecutor().submit(
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
