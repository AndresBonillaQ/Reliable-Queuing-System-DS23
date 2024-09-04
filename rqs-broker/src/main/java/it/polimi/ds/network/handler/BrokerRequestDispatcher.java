package it.polimi.ds.network.handler;

import it.polimi.ds.broker.BrokerContext;
import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.id.RequestIdEnum;
import it.polimi.ds.network.handler.impl.FollowerAppendValueRequestHandler;
import it.polimi.ds.network.handler.impl.FollowerCreateQueueRequestHandler;
import it.polimi.ds.network.handler.impl.FollowerReadValueRequestHandler;
import it.polimi.ds.utils.GsonInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class BrokerRequestDispatcher {

    private BrokerRequestDispatcher(){}

    private static final Map<RequestIdEnum, FollowerRequestHandler> requestHandlerMap;
    private static final Logger log = Logger.getLogger(BrokerRequestDispatcher.class.getName());

    static {
        requestHandlerMap = new ConcurrentHashMap<>();
        requestHandlerMap.put(RequestIdEnum.CREATE_QUEUE_REQUEST, new FollowerCreateQueueRequestHandler());
        requestHandlerMap.put(RequestIdEnum.APPEND_VALUE_REQUEST, new FollowerAppendValueRequestHandler());
        requestHandlerMap.put(RequestIdEnum.READ_VALUE_REQUEST, new FollowerReadValueRequestHandler());
    }

    public static ResponseMessage exec(BrokerContext brokerContext, String line) throws RequestNoManagedException {
        RequestMessage request = convertMessage(line);

        if(requestHandlerMap.containsKey(request.getId()))
            return requestHandlerMap.get(request.getId()).exec(brokerContext, request);

        log.warning("WARNING! the id " + request.getId() + " is not mapped in the responseDispatcher map, the message " + request + " has not been processed!");
        throw new RequestNoManagedException();
    }

    public static boolean isRequestNotManaged(String message){
        RequestMessage request = convertMessage(message);

        return !requestHandlerMap.containsKey(request.getId());
    }

    private static RequestMessage convertMessage(String message){
        return GsonInstance.getInstance().getGson().fromJson(message, RequestMessage.class);
    }
}
