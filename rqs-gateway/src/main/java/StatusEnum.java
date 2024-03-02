import java.util.Optional;

public enum StatusEnum {
    OK("OK"),
    KO("KO");

    private String value;

    StatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<StatusEnum> getStatusFromString(String value){
        for(StatusEnum ri : StatusEnum.values()){
            if(ri.value.equals(value))
                return Optional.of(ri);
        }

        return Optional.empty();
    }
}
