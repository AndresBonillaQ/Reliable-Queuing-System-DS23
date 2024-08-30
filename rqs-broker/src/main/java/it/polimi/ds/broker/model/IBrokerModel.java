package it.polimi.ds.broker.model;

import it.polimi.ds.exception.model.AlreadyExistsQueueWithSameIdException;
import it.polimi.ds.exception.model.EmptyQueueException;
import it.polimi.ds.exception.model.NoMoreValuesToReadInQueueException;
import it.polimi.ds.exception.model.QueueNotFoundException;

public interface IBrokerModel {
    /**
     * During new queue creation we check if there is no exists another queue with same ID,
     * otherwise create new queue with empty list associated (no data)
     * */
    void createNewQueue(String queueId) throws AlreadyExistsQueueWithSameIdException;

    /**
     * During queue value append we check if the queue exists, if exists we append value
     * */
    void appendValueToQueue(String queueId, int value) throws QueueNotFoundException;

    /**
     * During queue value append we check if the queue exists, if exists we read value.
     * If offsetMap doesn't contain the key client-queue its probable the first read of client on that queue
     * */
    int readValueFromQueueByClient(String queueId, String clientId) throws QueueNotFoundException, IndexOutOfBoundsException, EmptyQueueException, NoMoreValuesToReadInQueueException;

    void printState();
}
