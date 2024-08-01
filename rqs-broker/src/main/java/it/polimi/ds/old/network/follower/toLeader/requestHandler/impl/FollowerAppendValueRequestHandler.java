package it.polimi.ds.old.network.follower.toLeader.requestHandler.impl;

import it.polimi.ds.exception.model.QueueNotFoundException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.request.AppendValueRequest;
import it.polimi.ds.broker2.model.IBrokerModel;
import it.polimi.ds.old.network.follower.toLeader.requestHandler.FollowerRequestHandler;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.ResponseMessageBuilder;

import java.util.logging.Logger;

public class FollowerAppendValueRequestHandler implements FollowerRequestHandler {
    private final Logger log = Logger.getLogger(FollowerAppendValueRequestHandler.class.getName());

    @Override
    public ResponseMessage exec(IBrokerModel brokerModel, RequestMessage request) {
        AppendValueRequest appendValueRequest = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(request.getContent(), AppendValueRequest.class);

        try{
            brokerModel.appendValueToQueue(appendValueRequest.getQueueId(), appendValueRequest.getValue());
            return ResponseMessageBuilder.OK.buildAppendValueResponseMessage();
        } catch (QueueNotFoundException e){
            log.severe("Error during appending value! It doesn't exists the queue with ID " + appendValueRequest.getQueueId());
            return ResponseMessageBuilder.KO.buildAppendValueResponseMessage(Const.ResponseDes.KO.APPEND_VALUE_QUEUE_ID_NOT_EXISTS_KO);
        }
    }
}
