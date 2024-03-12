package it.polimi.ds.network.follower.toLeader.requestDispatcher;

import it.polimi.ds.exception.RequestNoManagedException;
import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.request.utils.RequestIdEnum;
import it.polimi.ds.model.IBrokerModel;
import it.polimi.ds.network.follower.toLeader.requestHandler.FollowerRequestHandler;
import it.polimi.ds.network.follower.toLeader.requestHandler.impl.FollowerAppendValueRequestHandler;
import it.polimi.ds.network.follower.toLeader.requestHandler.impl.FollowerCreateQueueRequestHandler;
import it.polimi.ds.network.follower.toLeader.requestHandler.impl.FollowerReadValueRequestHandler;
import it.polimi.ds.utils.GsonInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class FollowerRequestDispatcher {

    private static final Map<RequestIdEnum, FollowerRequestHandler> requestHandlerMap;
    private static final Logger log = Logger.getLogger(FollowerRequestDispatcher.class.getName());

    static{
        requestHandlerMap = new ConcurrentHashMap<>();
        requestHandlerMap.put(RequestIdEnum.CREATE_QUEUE_REQUEST, new FollowerCreateQueueRequestHandler());
        requestHandlerMap.put(RequestIdEnum.APPEND_VALUE_REQUEST, new FollowerAppendValueRequestHandler());
        requestHandlerMap.put(RequestIdEnum.READ_VALUE_REQUEST, new FollowerReadValueRequestHandler());
        requestHandlerMap.put(RequestIdEnum.COMMIT_REQUEST, new FollowerReadValueRequestHandler());
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
