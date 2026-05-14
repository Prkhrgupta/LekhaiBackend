package in.lekhai.gsp.ewb.domain.enums;

public enum SupplyType {

    INWARD("I", "Inward"),
    OUTWARD("O", "Outward");

    private final String code;
    private final String description;

    SupplyType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SupplyType fromCode(String code) {
        for (SupplyType value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                "Invalid SupplyType code: " + code
        );
    }
}