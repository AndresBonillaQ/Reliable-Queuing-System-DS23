package it.polimi.ds.network.leader.toGateway.requestHandler.impl;

import it.polimi.ds.exception.model.QueueNotFoundException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.request.AppendValueRequest;
import it.polimi.ds.model.IBrokerModel;
import it.polimi.ds.network.leader.toGateway.requestHandler.LeaderRequestHandler;
import it.polimi.ds.utils.Const;
import it.polimi.ds.utils.GsonInstance;
import it.polimi.ds.utils.ResponseMessageBuilder;

import java.util.logging.Logger;

public class LeaderAppendValueRequestHandler implements LeaderRequestHandler {
    private final Logger log = Logger.getLogger(LeaderAppendValueRequestHandler.class.getName());

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
