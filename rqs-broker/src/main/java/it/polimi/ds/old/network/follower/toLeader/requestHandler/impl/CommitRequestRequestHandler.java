package it.polimi.ds.old.network.follower.toLeader.requestHandler.impl;

import it.polimi.ds.exception.model.EmptyQueueException;
import it.polimi.ds.exception.model.QueueNotFoundException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.model.request.ReadValueRequest;
import it.polimi.ds.broker2.model.IBrokerModel;
import it.polimi.ds.old.network.follower.toLeader.requestHandler.FollowerRequestHandler;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.ModelResponseMessageBuilder;

import java.util.logging.Logger;

public class CommitRequestRequestHandler implements FollowerRequestHandler {
    private final Logger log = Logger.getLogger(CommitRequestRequestHandler.class.getName());

    @Override
    public ResponseMessage exec(IBrokerModel brokerModel, RequestMessage request) {
        ReadValueRequest readValueRequest = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(request.getContent(), ReadValueRequest.class);

        try{
            int valueRead = brokerModel.readValueFromQueueByClient(readValueRequest.getQueueId(), readValueRequest.getClientId());
            return ModelResponseMessageBuilder.OK.buildReadValueResponseMessage(valueRead);
        } catch (QueueNotFoundException e){
            log.severe("Error during reading value! It doesn't exists the queue with ID " + readValueRequest.getQueueId());
            return ModelResponseMessageBuilder.KO.buildReadValueResponseMessage(Const.ResponseDes.KO.READ_VALUE_QUEUE_ID_NOT_EXISTS_KO);
        } catch (IndexOutOfBoundsException e){
            //if here review logic of QueueState.readValueFromQueueByClient();
            log.severe("Error during reading value! Index out of bound in queue with ID " + readValueRequest.getQueueId());
            return ModelResponseMessageBuilder.KO.buildReadValueResponseMessage(Const.ResponseDes.KO.READ_VALUE_QUEUE_ID_INDEX_OUT_OF_BOUND_KO);
        } catch (EmptyQueueException e) {
            throw new RuntimeException(e);
        }
    }
}
