package it.polimi.ds.network.handler;

import it.polimi.ds.message.ResponseMessage;

public interface ResponseHandler {
    void exec(ResponseMessage message);
}
