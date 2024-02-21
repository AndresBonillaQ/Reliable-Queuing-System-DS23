package message.response;

import java.io.Serializable;

public class ReadValueResponse extends Response implements Serializable {
    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
