package it.polimi.ds.message.response.utils;

import java.util.Optional;

/**
 * Message Response identifier
 * */
public enum ResponseIdEnum {
    SET_UP_RESPONSE("setUpResp"),
    APPEND_VALUE_RESPONSE("appendValueResp"),
    CREATE_QUEUE_RESPONSE("createQueueResp"),
    READ_VALUE_RESPONSE("readValueResp"),
    HEARTBEAT_RESPONSE("heartbeatResp"),
    VOTE_OUTCOME("voteResponse"),;

    private final String value;

    ResponseIdEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<ResponseIdEnum> getResponseIdFromString(String value){
        for(ResponseIdEnum ri : ResponseIdEnum.values()){
            if(ri.value.equals(value))
                return Optional.of(ri);
        }

        return Optional.empty();
    }
}
