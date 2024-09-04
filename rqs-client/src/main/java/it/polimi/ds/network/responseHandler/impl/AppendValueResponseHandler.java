package it.polimi.ds.network.responseHandler.impl;

import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.response.AppendValueResponse;
import it.polimi.ds.network.responseHandler.ResponseHandler;
import it.polimi.ds.utils.GsonInstance;

public class AppendValueResponseHandler implements ResponseHandler {
    @Override
    public void exec(ResponseMessage message) {
        AppendValueResponse appendValueResponse = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(message.getContent(), AppendValueResponse.class);

        switch (appendValueResponse.getStatus()){
            case OK -> System.out.println("Value appended!");
            case KO -> System.out.println("Error appending value: " + appendValueResponse.getDesStatus());
        }
    }
}
