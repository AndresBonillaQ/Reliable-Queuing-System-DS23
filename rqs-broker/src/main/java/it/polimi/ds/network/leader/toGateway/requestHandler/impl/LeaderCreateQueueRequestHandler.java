package it.polimi.ds.network.leader.toGateway.requestHandler.impl;

import it.polimi.ds.exception.model.AlreadyExistsQueueWithSameIdException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.request.CreateQueueRequest;
import it.polimi.ds.model.IBrokerModel;
import it.polimi.ds.network.leader.toGateway.requestHandler.LeaderRequestHandler;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.ResponseMessageBuilder;

import java.util.logging.Logger;

public class LeaderCreateQueueRequestHandler implements LeaderRequestHandler {
    private final Logger log = Logger.getLogger(LeaderCreateQueueRequestHandler.class.getName());

    @Override
    public ResponseMessage exec(IBrokerModel brokerModel, RequestMessage request) {
        CreateQueueRequest createQueueRequest = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(request.getContent(), CreateQueueRequest.class);

        try{
            brokerModel.createNewQueue(createQueueRequest.getQueueId());
            return ResponseMessageBuilder.OK.buildCreateQueueResponseMessage();
        } catch (AlreadyExistsQueueWithSameIdException e){
            log.severe("Error during create queue! It already exists a queue with the ID " + createQueueRequest.getQueueId());
            return ResponseMessageBuilder.KO.buildCreateQueueResponseMessage(Const.ResponseDes.KO.CREATE_QUEUE_QUEUE_ID_ALREADY_PRESENT_KO);
        }
    }
}