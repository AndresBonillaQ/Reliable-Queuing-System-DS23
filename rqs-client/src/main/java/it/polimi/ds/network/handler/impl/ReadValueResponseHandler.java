package it.polimi.ds.network.handler.impl;

import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.response.ReadValueResponse;
import it.polimi.ds.network.handler.ResponseHandler;
import it.polimi.ds.utils.GsonInstance;

public class ReadValueResponseHandler implements ResponseHandler {
    @Override
    public void exec(ResponseMessage message) {
        ReadValueResponse readValueResponse = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(message.getContent(), ReadValueResponse.class);

        switch (readValueResponse.getStatus()){
            case OK -> System.out.println("Read value: " + readValueResponse.getValue());
            case KO -> System.out.println("Error reading value: " + readValueResponse.getDesStatus());
        }
    }
}
