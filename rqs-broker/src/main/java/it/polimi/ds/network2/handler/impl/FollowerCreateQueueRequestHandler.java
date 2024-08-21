package it.polimi.ds.network2.handler.impl;

import it.polimi.ds.broker2.BrokerContext;
import it.polimi.ds.broker2.model.IBrokerModel;
import it.polimi.ds.exception.model.AlreadyExistsQueueWithSameIdException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.model.request.CreateQueueRequest;
import it.polimi.ds.network2.handler.FollowerRequestHandler;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.ModelResponseMessageBuilder;

import java.util.logging.Logger;

public class FollowerCreateQueueRequestHandler implements FollowerRequestHandler {
    private final Logger log = Logger.getLogger(FollowerCreateQueueRequestHandler.class.getName());

    @Override
    public ResponseMessage exec(BrokerContext brokerContext, RequestMessage request) {
        CreateQueueRequest createQueueRequest = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(request.getContent(), CreateQueueRequest.class);

        try{
            brokerContext.getBrokerModel().createNewQueue(createQueueRequest.getQueueId());
            return ModelResponseMessageBuilder.OK.buildCreateQueueResponseMessage();
        } catch (AlreadyExistsQueueWithSameIdException e){
            log.severe("Error during create queue! It already exists a queue with the ID " + createQueueRequest.getQueueId());
            return ModelResponseMessageBuilder.KO.buildCreateQueueResponseMessage(Const.ResponseDes.KO.CREATE_QUEUE_QUEUE_ID_ALREADY_PRESENT_KO);
        }
    }
}
