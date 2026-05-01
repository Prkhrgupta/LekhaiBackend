package in.lekhai.gsp.ewb.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransportMode {

    ROAD("1", in.lekhai.contract.model.TransportMode.ROAD),
    RAIL("2", in.lekhai.contract.model.TransportMode.RAIL),
    AIR("3", in.lekhai.contract.model.TransportMode.AIR),
    SHIP("4", in.lekhai.contract.model.TransportMode.SHIP),
    IN_TRANSIT("5", in.lekhai.contract.model.TransportMode.IN_TRANSIT);

    private final String code;
    private final in.lekhai.contract.model.TransportMode contractTransportMode;

    TransportMode(String code,
                  in.lekhai.contract.model.TransportMode contractTransportMode) {
        this.code = code;
        this.contractTransportMode = contractTransportMode;
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

    public in.lekhai.contract.model.TransportMode getContractTransportMode() {
        return contractTransportMode;
    }
}