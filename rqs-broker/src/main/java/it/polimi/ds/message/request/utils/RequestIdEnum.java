package it.polimi.ds.message.request.utils;

import java.util.Optional;

/**
 * Message Response identifier
 * */
public enum RequestIdEnum {
    APPEND_VALUE_REQUEST("appendValueReq"),
    CREATE_QUEUE_REQUEST("createQueueReq"),
    READ_VALUE_REQUEST("readValueReq"),
    HEARTBEAT_REQUEST("heartbeatReq"),
    COMMIT_REQUEST("commitReq"),
    APPEND_ENTRY_LOG_REQUEST("appendEntryLogReq");

    private final String value;

    RequestIdEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<RequestIdEnum> getRequestIdFromString(String value){
        for(RequestIdEnum ri : RequestIdEnum.values()){
            if(ri.value.equals(value))
                return Optional.of(ri);
        }

        return Optional.empty();
    }
}
