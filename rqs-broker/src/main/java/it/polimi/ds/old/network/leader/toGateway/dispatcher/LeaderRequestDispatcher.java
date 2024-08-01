package it.polimi.ds.old.network.leader.toGateway.dispatcher;

import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.request.utils.RequestIdEnum;
import it.polimi.ds.broker2.model.IBrokerModel;
import it.polimi.ds.old.network.leader.toGateway.requestHandler.impl.LeaderAppendValueRequestHandler;
import it.polimi.ds.old.network.leader.toGateway.requestHandler.impl.LeaderCreateQueueRequestHandler;
import it.polimi.ds.old.network.leader.toGateway.requestHandler.impl.LeaderReadValueRequestHandler;
import it.polimi.ds.old.network.leader.toGateway.requestHandler.LeaderRequestHandler;
import it.polimi.ds.utils.GsonInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class LeaderRequestDispatcher {

    private static final Map<RequestIdEnum, LeaderRequestHandler> requestHandlerMap;
    private static final Logger log = Logger.getLogger(LeaderRequestDispatcher.class.getName());

    static{
        requestHandlerMap = new ConcurrentHashMap<>();
        requestHandlerMap.put(RequestIdEnum.CREATE_QUEUE_REQUEST, new LeaderCreateQueueRequestHandler());
        requestHandlerMap.put(RequestIdEnum.APPEND_VALUE_REQUEST, new LeaderAppendValueRequestHandler());
        requestHandlerMap.put(RequestIdEnum.READ_VALUE_REQUEST, new LeaderReadValueRequestHandler());
    }

    public static ResponseMessage exec(IBrokerModel brokerModel, String line) throws RequestNoManagedException {
        RequestMessage request = convertMessage(line);

        if(requestHandlerMap.containsKey(request.getId()))
            return requestHandlerMap.get(request.getId()).exec(brokerModel, request);

        log.warning("WARNING! the id " + request.getId() + " is not mapped in the responseDispatcher map, the message " + request + " has not been processed!");
        throw new RequestNoManagedException();
    }

    private static RequestMessage convertMessage(String message){
        return GsonInstance.getInstance().getGson().fromJson(message, RequestMessage.class);
    }
}
