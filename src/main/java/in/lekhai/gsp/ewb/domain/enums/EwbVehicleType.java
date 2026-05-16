package in.lekhai.gsp.ewb.domain.enums;


public enum EwbVehicleType {

    R("R", "Regular"),

    O("O", "ODC (Over Dimensional Cargo)");

    private final String code;

    private final String description;

    EwbVehicleType(String code,
                   String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EwbVehicleType fromCode(String code) {

        if (code == null || code.isBlank()) {
            return null;
        }

        for (EwbVehicleType value : values()) {
            if (value.code.equalsIgnoreCase(code.trim())) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                "Invalid EwbVehicleType code: " + code
        );
    }
}