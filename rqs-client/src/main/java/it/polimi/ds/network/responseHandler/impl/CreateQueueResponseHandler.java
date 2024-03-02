package it.polimi.ds.network.responseHandler.impl;

import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.response.CreateQueueResponse;
import it.polimi.ds.network.responseHandler.ResponseHandler;
import it.polimi.ds.utils.GsonInstance;

public class CreateQueueResponseHandler implements ResponseHandler {
    @Override
    public void exec(ResponseMessage message) {
        CreateQueueResponse createQueueResponse = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(message.getContent(), CreateQueueResponse.class);

        switch (createQueueResponse.getStatus()){
            case OK -> System.out.println("Queue created with ID: " + createQueueResponse.getQueueId());
            case KO -> System.out.println("Error creating queue: " + createQueueResponse.getDesStatus());
        }
    }
}
