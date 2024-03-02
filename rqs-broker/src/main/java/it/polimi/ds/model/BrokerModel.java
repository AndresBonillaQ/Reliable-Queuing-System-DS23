package it.polimi.ds.model;

import it.polimi.ds.exception.model.AlreadyExistsQueueWithSameIdException;
import it.polimi.ds.exception.model.QueueNotFoundException;
import it.polimi.ds.utils.ClientQueueId;

import java.util.*;

public class BrokerModel implements IBrokerModel {

    /**
     * Data structure to store queues, each queue is identified by his ID
     * */
    private final Map<String, List<Integer>> queuesMap = new HashMap<>();

    /**
     * Data structure to store offset of each client, to each clientId-queueId is associated an offset
     * */
    private final Map<ClientQueueId, Integer> clientOffsetMap = new HashMap<>();

    public void createNewQueue(String queueId) throws AlreadyExistsQueueWithSameIdException {
        if(queuesMap.containsKey(queueId))
            throw new AlreadyExistsQueueWithSameIdException();

        queuesMap.put(queueId, new ArrayList<>());
    }

    public void appendValueToQueue(String queueId, int value) throws QueueNotFoundException {
        if(!queuesMap.containsKey(queueId))
            throw new QueueNotFoundException();

        queuesMap.get(queueId).add(value);
    }

    public int readValueFromQueueByClient(String queueId, String clientId) throws QueueNotFoundException, IndexOutOfBoundsException {
        if(!queuesMap.containsKey(queueId))
            throw new QueueNotFoundException();

        ClientQueueId offsetKey = new ClientQueueId(clientId, queueId);

        int oldOffset = Objects.isNull(clientOffsetMap.get(offsetKey)) ? 0 : clientOffsetMap.get(offsetKey);

        //if continue to read last value we stop the offset because there are no other values to read!
        int newOffset = queuesMap.get(queueId).size() - 1 > oldOffset ? oldOffset + 1 : queuesMap.get(queueId).size() - 1;

        //update offset
        clientOffsetMap.put(offsetKey, newOffset);

        //return value read
        return queuesMap.get(queueId).get(oldOffset);
    }
}
