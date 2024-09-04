package it.polimi.ds.network.responseHandler;

import it.polimi.ds.message.ResponseMessage;

public interface ResponseHandler {
    void exec(ResponseMessage message);
}
