import it.polimi.ds.broker.model.impl.BrokerModel;
import it.polimi.ds.exception.model.AlreadyExistsQueueWithSameIdException;
import it.polimi.ds.exception.model.EmptyQueueException;
import it.polimi.ds.exception.model.NoMoreValuesToReadInQueueException;
import it.polimi.ds.exception.model.QueueNotFoundException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class BrokerModelTest {

    @Test
    public void createNewQueue() throws AlreadyExistsQueueWithSameIdException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");
        Assert.assertNotNull(brokerModel.getQueuesMap().get("1"));
    }

    @Test
    public void createNewQueue2() throws AlreadyExistsQueueWithSameIdException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");
        Assert.assertNull(brokerModel.getQueuesMap().get("2"));
    }

    @Test
    public void createNewQueue3() throws AlreadyExistsQueueWithSameIdException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");
        Assert.assertThrows(AlreadyExistsQueueWithSameIdException.class, () -> brokerModel.createNewQueue("1"));
    }

    @Test
    public void appendValueToQueue() throws AlreadyExistsQueueWithSameIdException, QueueNotFoundException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");
        brokerModel.appendValueToQueue("1", 1);
        brokerModel.appendValueToQueue("1", 2);
        brokerModel.appendValueToQueue("1", 3);
        Assert.assertEquals(brokerModel.getQueuesMap().get("1"), List.of(1, 2, 3));
    }

    @Test
    public void appendValueToQueue2() throws AlreadyExistsQueueWithSameIdException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");
        Assert.assertThrows(QueueNotFoundException.class, () -> brokerModel.appendValueToQueue("2", 3));
    }

    @Test
    public void readValueFromQueueByClient() throws AlreadyExistsQueueWithSameIdException, QueueNotFoundException, EmptyQueueException, NoMoreValuesToReadInQueueException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");
        brokerModel.appendValueToQueue("1", 1);
        brokerModel.appendValueToQueue("1", 2);
        brokerModel.appendValueToQueue("1", 3);

        int val = brokerModel.readValueFromQueueByClient("1", "1");

        Assert.assertEquals(val, 1);
    }

    @Test
    public void readValueFromQueueByClient2() throws AlreadyExistsQueueWithSameIdException, QueueNotFoundException, EmptyQueueException, NoMoreValuesToReadInQueueException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");
        brokerModel.appendValueToQueue("1", 1);
        brokerModel.appendValueToQueue("1", 2);

        int val = brokerModel.readValueFromQueueByClient("1", "1");
        val = brokerModel.readValueFromQueueByClient("1", "1");

        Assert.assertEquals(val, 2);
    }

    @Test
    public void readValueFromQueueByClient3() throws AlreadyExistsQueueWithSameIdException, QueueNotFoundException, EmptyQueueException, NoMoreValuesToReadInQueueException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");
        brokerModel.appendValueToQueue("1", 1);
        brokerModel.appendValueToQueue("1", 2);

        int val = brokerModel.readValueFromQueueByClient("1", "1");
        val = brokerModel.readValueFromQueueByClient("1", "1");

        Assert.assertEquals(val, 2);
    }

    @Test
    public void readValueFromQueueByClient4() throws AlreadyExistsQueueWithSameIdException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");

        Assert.assertThrows(EmptyQueueException.class, () ->  brokerModel.readValueFromQueueByClient("1", "1"));
    }

    @Test
    public void readValueFromQueueByClient5() throws AlreadyExistsQueueWithSameIdException {
        BrokerModel brokerModel = new BrokerModel();
        brokerModel.createNewQueue("1");

        Assert.assertThrows(QueueNotFoundException.class, () ->  brokerModel.readValueFromQueueByClient("2", "1"));
    }

}
