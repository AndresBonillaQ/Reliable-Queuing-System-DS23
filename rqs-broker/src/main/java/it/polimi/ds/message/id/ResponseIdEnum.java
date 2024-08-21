package it.polimi.ds.message.id;

import java.util.Optional;

/**
 * Message Response identifier
 * */
public enum ResponseIdEnum {

    // Model
    APPEND_VALUE_RESPONSE("appendValueResp"),
    CREATE_QUEUE_RESPONSE("createQueueResp"),
    READ_VALUE_RESPONSE("readValueResp"),

    // Raft
    SET_UP_RESPONSE("setUpResp"),
    HEARTBEAT_RESPONSE("heartbeatResp"),
    COMMIT_RESPONSE("commitResp"),
    APPEND_ENTRY_LOG_RESPONSE("appendEntryLogResp"),
    REQUEST_NOT_MANAGED("requestNotManaged");

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
