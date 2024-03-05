package messages.responses;

import java.io.Serializable;


public class ReadValueResponse implements Serializable {
    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
