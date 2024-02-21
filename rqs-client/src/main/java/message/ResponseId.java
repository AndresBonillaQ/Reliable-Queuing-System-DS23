package message;

import java.util.Optional;

/**
 * Message Response identifier
 * */
public enum ResponseId {
    APPEND_VALUE_RESPONSE("appendValue"),
    CREATE_QUEUE_RESPONSE("createQueue"),
    READ_VALUE_RESPONSE("readValue");

    private final String value;

    ResponseId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Optional<ResponseId> getResponseIdFromString(String value){
        for(ResponseId ri : ResponseId.values()){
            if(ri.value.equals(value))
                return Optional.of(ri);
        }

        return Optional.empty();
    }
}
