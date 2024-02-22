package it.polimi.ds.network.dispatcher;

import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.response.ResponseIdEnum;
import it.polimi.ds.network.handler.ResponseHandler;
import it.polimi.ds.network.handler.impl.AppendValueResponseHandler;
import it.polimi.ds.network.handler.impl.CreateQueueResponseHandler;
import it.polimi.ds.network.handler.impl.ReadValueResponseHandler;
import it.polimi.ds.utils.GsonInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Dispatching messages based on messageId of message, each implementation of MessageHandler will implement deserialization
 * */
public class ResponseDispatcher {

    private static final Map<ResponseIdEnum, ResponseHandler> messageHandlerMap;
    private static final Logger log = Logger.getLogger(ResponseDispatcher.class.getName());

    static{
        messageHandlerMap = new ConcurrentHashMap<>();
        messageHandlerMap.put(ResponseIdEnum.CREATE_QUEUE_RESPONSE, new CreateQueueResponseHandler());
        messageHandlerMap.put(ResponseIdEnum.APPEND_VALUE_RESPONSE, new AppendValueResponseHandler());
        messageHandlerMap.put(ResponseIdEnum.READ_VALUE_RESPONSE, new ReadValueResponseHandler());
    }

    private ResponseDispatcher(){}

    /**
     * We convert line into Message and check
     *  -   if messageId is within enum ResponseId
     *  -   if enum associated is within messageHandlerMap
     * */
    public static void exec(String line){
        ResponseMessage message = convertMessage(line);

        if(messageHandlerMap.containsKey(message.getId())){
            messageHandlerMap.get(message.getId()).exec(message);
        } else {
            log.warning("WARNING! the id " + message.getId() + " is not mapped in the responseDispatcher map, the message " + message + " has not been processed!");
        }
    }

    private static ResponseMessage convertMessage(String message){
        return GsonInstance.getInstance().getGson().fromJson(message, ResponseMessage.class);
    }
}
