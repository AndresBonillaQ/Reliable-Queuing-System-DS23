package it.polimi.ds.network.handler.impl;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.exception.model.EmptyQueueException;
import it.polimi.ds.exception.model.NoMoreValuesToReadInQueueException;
import it.polimi.ds.exception.model.QueueNotFoundException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.model.request.ReadValueRequest;
import it.polimi.ds.network.handler.FollowerRequestHandler;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.builder.ModelResponseMessageBuilder;

import java.util.logging.Level;
import java.util.logging.Logger;
public class FollowerReadValueRequestHandler implements FollowerRequestHandler {

    private final Logger log = Logger.getLogger(FollowerReadValueRequestHandler.class.getName());

    @Override
    public ResponseMessage exec(BrokerContext brokerContext, RequestMessage request) {
        ReadValueRequest readValueRequest = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(request.getContent(), ReadValueRequest.class);

        try{
            int valueRead = brokerContext.getBrokerModel().readValueFromQueueByClient(readValueRequest.getQueueId(), request.getClientId());
            return ModelResponseMessageBuilder.OK.buildReadValueResponseMessage(valueRead, request.getClientId());
        } catch (QueueNotFoundException e){
            log.severe("Error during reading value! It doesn't exists the queue with ID " + readValueRequest.getQueueId());
            return ModelResponseMessageBuilder.KO.buildReadValueResponseMessage(request.getClientId(), Const.ResponseDes.KO.READ_VALUE_QUEUE_ID_NOT_EXISTS_KO);
        } catch (IndexOutOfBoundsException e){
            //if here review logic of QueueState.readValueFromQueueByClient();
            log.severe("Error during reading value! Index out of bound in queue with ID " + readValueRequest.getQueueId());
            return ModelResponseMessageBuilder.KO.buildReadValueResponseMessage(request.getClientId(), Const.ResponseDes.KO.READ_VALUE_QUEUE_ID_INDEX_OUT_OF_BOUND_KO);
        } catch (EmptyQueueException e) {
            log.severe("Error during reading value! The queue is empty, ID " + readValueRequest.getQueueId());
            return ModelResponseMessageBuilder.KO.buildReadValueResponseMessage(request.getClientId(), Const.ResponseDes.KO.READ_VALUE_QUEUE_EMPTY_KO);
        } catch (NoMoreValuesToReadInQueueException e) {
            log.log(Level.SEVERE, "Error during reading value! The client {0} has read all values of queueId {1}", new Object[]{request.getClientId(), readValueRequest.getQueueId()});
            return ModelResponseMessageBuilder.KO.buildReadValueResponseMessage(request.getClientId(), Const.ResponseDes.KO.READ_VALUE_QUEUE_NO_MORE_VALUES_TO_READ_KO);
        }
    }
}
