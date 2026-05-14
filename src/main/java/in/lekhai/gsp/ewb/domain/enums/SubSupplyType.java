package in.lekhai.gsp.ewb.domain.enums;

public enum SubSupplyType {

    SUPPLY("1", "Supply"),
    IMPORT("2", "Import"),
    EXPORT("3", "Export"),
    JOB_WORK("4", "Job Work"),
    FOR_OWN_USE("5", "For Own Use"),
    JOB_WORK_RETURNS("6", "Job Work Returns"),
    SALES_RETURN("7", "Sales Return"),
    OTHERS("8", "Others"),
    SKD_CKD_LOTS("9", "SKD/CKD/Lots"),
    LINE_SALES("10", "Line Sales"),
    RECIPIENT_NOT_KNOWN("11", "Recipient Not Known"),
    EXHIBITION_OR_FAIRS("12", "Exhibition or Fairs");

    private final String code;
    private final String description;

    SubSupplyType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static SubSupplyType fromCode(String code) {
        for (SubSupplyType value : values()) {
            if (value.code.equals(code.trim())) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                "Invalid SubSupplyType code: " + code
        );
    }
}