package it.polimi.ds.network.responseHandler.impl;

import it.polimi.ds.message.ResponseMessage;
import it.polimi.ds.message.response.ServiceUnavailableResponse;
import it.polimi.ds.network.responseHandler.ResponseHandler;
import it.polimi.ds.utils.GsonInstance;

public class ServiceUnavailableHandler implements ResponseHandler {

    @Override
    public void exec(ResponseMessage message) {
        ServiceUnavailableResponse serviceUnavailableResponse = GsonInstance
                .getInstance()
                .getGson()
                .fromJson(message.getContent(), ServiceUnavailableResponse.class);

        System.out.println("Error: " + serviceUnavailableResponse.getDesStatus());
    }
}
