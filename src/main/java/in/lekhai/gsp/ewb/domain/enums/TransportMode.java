package in.lekhai.gsp.ewb.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransportMode {

    ROAD("1"),
    RAIL("2"),
    AIR("3"),
    SHIP("4"),
    IN_TRANSIT("5");

    private final String code;

    TransportMode(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static TransportMode fromCode(String code) {
        for (TransportMode mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unknown transportation mode: " + code);
    }
}