package it.polimi.ds.broker.model.impl;

import it.polimi.ds.broker.model.IBrokerModel;
import it.polimi.ds.exception.model.AlreadyExistsQueueWithSameIdException;
import it.polimi.ds.exception.model.EmptyQueueException;
import it.polimi.ds.exception.model.QueueNotFoundException;
import it.polimi.ds.broker.model.utils.ClientQueueId;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains the information stored by a cluster, which is replicated among all the cluster's followers.
 * */
public class BrokerModel implements IBrokerModel{

    /**
     * Data structure to store queues, each queue is identified by his ID
     * */
    private final Map<String, List<Integer>> queuesMap = new HashMap<>();

    /**
     * Data structure to store offset of each client, to each clientId-queueId is associated an offset
     * */
    private final Map<ClientQueueId, Integer> clientOffsetMap = new HashMap<>();

    private final Logger log = Logger.getLogger(BrokerModel.class.getName());

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

    public int readValueFromQueueByClient(String queueId, String clientId) throws QueueNotFoundException, EmptyQueueException, IndexOutOfBoundsException {
        if(!queuesMap.containsKey(queueId))
            throw new QueueNotFoundException();

        if(queuesMap.get(queueId).isEmpty())
            throw new EmptyQueueException();

        ClientQueueId offsetKey = new ClientQueueId(clientId, queueId);

        // if first time the client read the value, start from 0, otherwise value is stored in the map
        int oldOffset = Objects.isNull(clientOffsetMap.get(offsetKey)) ? 0 : clientOffsetMap.get(offsetKey);
        System.out.println("oldOffset: " + oldOffset);

        //if continue to read last value we stop the offset because there are no other values to read!
        if(oldOffset == queuesMap.get(queueId).size() - 1){
            log.log(Level.INFO, "Client {0} reached the top of queue {1}", new Object[]{clientId, queueId});
            return queuesMap.get(queueId).get(oldOffset);
        }

        int newOffset = oldOffset + 1;
        clientOffsetMap.put(offsetKey, newOffset);

        log.log(Level.INFO, "Client {0} reading... oldOffset was {1}, the new one is {2}", new Object[]{clientId, oldOffset, newOffset});

        //return value read
        return queuesMap.get(queueId).get(oldOffset);
    }

    @Override
    public void printState() {/*
        System.out.println("After execution of request:");
        System.out.println("+---------+-------------------------+");
        System.out.println("| QueueID | Elements                |");
        System.out.println("+---------+-------------------------+");

        for (Map.Entry<String, List<Integer>> entry : queuesMap.entrySet()) {
            String queueId = entry.getKey();
            String elements = entry.getValue().toString();

            System.out.printf("| %-7s | %-23s |\n", queueId, elements);
        }

        System.out.println("+---------+-------------------------+");

        System.out.println("+-------------+-------------+---------+");
        System.out.println("| Client ID   | Queue ID    | Offset  |");
        System.out.println("+-------------+-------------+---------+");

        for (Map.Entry<ClientQueueId, Integer> entry : clientOffsetMap.entrySet()) {
            ClientQueueId clientQueueId = entry.getKey();
            Integer offset = entry.getValue();

            System.out.printf("| %-11s | %-11s | %-7d |\n",
                    clientQueueId.getClientId(),
                    clientQueueId.getQueueId(),
                    offset);
        }

        System.out.println("+-------------+-------------+---------+");*/
    }

    public Map<String, List<Integer>> getQueuesMap() {
        return queuesMap;
    }

    public Map<ClientQueueId, Integer> getClientOffsetMap() {
        return clientOffsetMap;
    }
}
