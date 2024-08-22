package it.polimi.ds.message.model.response.utils;

import java.util.Optional;

public enum DesStatusEnum {
    CREATE_QUEUE_OK("The queue has been successfully created!"),
    APPEND_VALUE_OK("The value has been successfully appended!"),
    READ_VALUE_OK("The value has been successfully read:"),
    SET_UP_OK("The client has connect successfully"),
    RAFT_LOG_ENTRY_OK("The log has append successfully"),

    CREATE_QUEUE_QUEUE_ID_ALREADY_PRESENT_KO("The queue has been impossible to create because the queueId it's already used"),
    APPEND_VALUE_QUEUE_ID_NOT_EXISTS_KO("The value has been impossible to append because the queueId doesn't exists"),
    READ_VALUE_QUEUE_ID_NOT_EXISTS_KO("The values has been impossible to read because the queueId doesn't exists"),
    SET_UP_KO("The client has not connect successfully"),
    RAFT_LOG_ENTRY_KO("The log has not append successfully")
    ;

    private final String value;

    DesStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<DesStatusEnum> getStatusFromString(String value){
        for(DesStatusEnum ri : DesStatusEnum.values()){
            if(ri.value.equals(value))
                return Optional.of(ri);
        }

        return Optional.empty();
    }
}
