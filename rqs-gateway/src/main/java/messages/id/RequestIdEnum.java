package messages.id;


import java.util.Optional;

/**
 * Message utils.Response identifier
 * */
public enum RequestIdEnum {
    APPEND_VALUE_REQUEST("appendValueReq"),
    CREATE_QUEUE_REQUEST("createQueueReq"),
    READ_VALUE_REQUEST("readValueReq"),
    NEW_LEADER_TO_GATEWAY_REQUEST("newLeaderToGatewayReq"),
    SET_UP_REQUEST("setUpReq"),
    PING_PONG_REQUEST("pingPongReq");

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
