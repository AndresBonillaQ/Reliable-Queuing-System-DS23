package network.brokerCommunication.client.reconnection;
import java.util.concurrent.CountDownLatch;

public class Notify {

    private final CountDownLatch latch = new CountDownLatch(1);
    private String clusterId;
    public Notify(String clusterId) {
        this.clusterId = clusterId;
    }

        public void waitForEvent() throws InterruptedException {
            latch.await();
        }

        public void signalEvent() {
            latch.countDown();
        }
}
