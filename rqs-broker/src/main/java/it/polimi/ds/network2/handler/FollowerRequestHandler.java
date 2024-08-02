package it.polimi.ds.network2.handler;

import it.polimi.ds.broker2.model.IBrokerModel;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;

public interface FollowerRequestHandler {
    ResponseMessage exec(IBrokerModel brokerState, RequestMessage message);
}
