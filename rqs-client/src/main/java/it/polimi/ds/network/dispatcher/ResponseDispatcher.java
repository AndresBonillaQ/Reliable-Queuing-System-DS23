package it.polimi.ds.network.dispatcher;

import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.response.ResponseIdEnum;
import it.polimi.ds.network.responseHandler.ResponseHandler;
import it.polimi.ds.network.responseHandler.impl.AppendValueResponseHandler;
import it.polimi.ds.network.responseHandler.impl.CreateQueueResponseHandler;
import it.polimi.ds.network.responseHandler.impl.ReadValueResponseHandler;
import it.polimi.ds.network.responseHandler.impl.ServiceUnavailableHandler;
import it.polimi.ds.utils.GsonInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Dispatching messages based on messageId of message, each implementation of MessageHandler will implement deserialization
 * */
public class ResponseDispatcher {

    private static final Map<ResponseIdEnum, ResponseHandler> responseHandlerMap;
    private static final Logger log = Logger.getLogger(ResponseDispatcher.class.getName());

    static{
        responseHandlerMap = new ConcurrentHashMap<>();
        responseHandlerMap.put(ResponseIdEnum.CREATE_QUEUE_RESPONSE, new CreateQueueResponseHandler());
        responseHandlerMap.put(ResponseIdEnum.APPEND_VALUE_RESPONSE, new AppendValueResponseHandler());
        responseHandlerMap.put(ResponseIdEnum.READ_VALUE_RESPONSE, new ReadValueResponseHandler());
        responseHandlerMap.put(ResponseIdEnum.SERVICE_UNAVAILABLE_RESPONSE, new ServiceUnavailableHandler());
    }

    private ResponseDispatcher(){}

    /**
     * We convert line into Message and check
     *  -   if messageId is within enum ResponseId
     *  -   if enum associated is within messageHandlerMap
     * */
    public static void exec(String line){
        ResponseMessage response = convertMessage(line);
        if(responseHandlerMap.containsKey(response.getId())){
            responseHandlerMap.get(response.getId()).exec(response);
        } else {
            log.warning("WARNING! the id " + response.getId() + " is not mapped in the responseDispatcher map, the message " + response + " has not been processed!");
        }
    }

    private static ResponseMessage convertMessage(String message){
        return GsonInstance.getInstance().getGson().fromJson(message, ResponseMessage.class);
    }
}
