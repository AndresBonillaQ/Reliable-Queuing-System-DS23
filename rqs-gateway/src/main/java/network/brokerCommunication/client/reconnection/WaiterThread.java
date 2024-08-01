package network.brokerCommunication.client.reconnection;

public class WaiterThread extends Thread {
    private final Notify notify;
    public WaiterThread(Notify notify) {
        this.notify = notify;
    }
    public void run() {
        try {
            notify.waitForEvent();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread interrupted");
        }
    }
}

