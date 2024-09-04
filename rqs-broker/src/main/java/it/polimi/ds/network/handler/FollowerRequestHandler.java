package it.polimi.ds.network.handler;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;

public interface FollowerRequestHandler {
    ResponseMessage exec(BrokerContext brokerContext, RequestMessage message);

}
