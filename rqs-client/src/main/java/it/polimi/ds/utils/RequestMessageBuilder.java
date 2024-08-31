package it.polimi.ds.utils;

import it.polimi.ds.message.RequestMessage;
import it.polimi.ds.message.request.*;

public class RequestMessageBuilder {
    public static RequestMessage buildAppendValueRequestMessage(String clientId, String queueId, int value){
        AppendValueRequest appendValueRequest = new AppendValueRequest(queueId, value);

        return new RequestMessage(
                RequestIdEnum.APPEND_VALUE_REQUEST,
                GsonInstance.getInstance().getGson().toJson(appendValueRequest),
                clientId
        );
    }

    public static RequestMessage buildReadValueRequestMessage(String clientId, String queueId){
        ReadValueRequest readValueRequest = new ReadValueRequest(queueId);

        return new RequestMessage(
                RequestIdEnum.READ_VALUE_REQUEST,
                GsonInstance.getInstance().getGson().toJson(readValueRequest),
                clientId
        );
    }

    public static RequestMessage buildCreateQueueRequestMessage(String clientId){
        CreateQueueRequest createQueueRequest = new CreateQueueRequest();

        return new RequestMessage(
                RequestIdEnum.CREATE_QUEUE_REQUEST,
                GsonInstance.getInstance().getGson().toJson(createQueueRequest),
                clientId
        );
    }

    public static RequestMessage buildSetUpRequestMessage(String clientId){
        SetUpRequest setUpRequest = new SetUpRequest();

        return new RequestMessage(
                RequestIdEnum.SET_UP_REQUEST,
                GsonInstance.getInstance().getGson().toJson(setUpRequest),
                clientId
        );
    }

    private RequestMessageBuilder(){}
}
