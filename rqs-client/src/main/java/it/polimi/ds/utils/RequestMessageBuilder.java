package it.polimi.ds.utils;

import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.request.AppendValueRequest;
import it.polimi.ds.message.request.CreateQueueRequest;
import it.polimi.ds.message.request.ReadValueRequest;
import it.polimi.ds.message.request.RequestIdEnum;

public class RequestMessageBuilder {
    public static RequestMessage buildAppendValueRequestMessage(String clientId, String queueId, int value){
        AppendValueRequest appendValueRequest = new AppendValueRequest();
        appendValueRequest.setClientId(clientId);
        appendValueRequest.setQueueId(queueId);
        appendValueRequest.setValue(value);

        RequestMessage message = new RequestMessage();
        message.setId(RequestIdEnum.APPEND_VALUE_REQUEST);
        message.setContent(GsonInstance.getInstance().getGson().toJson(appendValueRequest));

        return message;
    }

    public static RequestMessage buildReadValueRequestMessage(String clientId, String queueId){
        ReadValueRequest readValueRequest = new ReadValueRequest();
        readValueRequest.setClientId(clientId);
        readValueRequest.setQueueId(queueId);

        RequestMessage message = new RequestMessage();
        message.setId(RequestIdEnum.READ_VALUE_REQUEST);
        message.setContent(GsonInstance.getInstance().getGson().toJson(readValueRequest));

        return message;
    }

    public static RequestMessage buildCreareQueueRequestMessage(String clientId){
        CreateQueueRequest createQueueRequest = new CreateQueueRequest();
        createQueueRequest.setClientId(clientId);

        RequestMessage message = new RequestMessage();
        message.setId(RequestIdEnum.CREATE_QUEUE_REQUEST);
        message.setContent(GsonInstance.getInstance().getGson().toJson(createQueueRequest));

        return message;
    }

    private RequestMessageBuilder(){}
}
