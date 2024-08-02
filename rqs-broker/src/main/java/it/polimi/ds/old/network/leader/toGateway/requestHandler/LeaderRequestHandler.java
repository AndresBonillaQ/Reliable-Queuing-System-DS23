package it.polimi.ds.old.network.leader.toGateway.requestHandler;

import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.broker2.model.IBrokerModel;

public interface LeaderRequestHandler {
    ResponseMessage exec(IBrokerModel brokerState, RequestMessage message);
}
