package it.polimi.ds.network.leader.toGateway.requestHandler;

import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.model.IBrokerModel;

public interface LeaderRequestHandler {
    ResponseMessage exec(IBrokerModel brokerState, RequestMessage message);
}
