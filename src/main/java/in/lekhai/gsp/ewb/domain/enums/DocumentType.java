package in.lekhai.gsp.ewb.domain.enums;

public enum DocumentType {

    TAX_INVOICE("INV", "Tax Invoice"),
    BILL_OF_SUPPLY("BIL", "Bill of Supply"),
    BILL_OF_ENTRY("BOE", "Bill of Entry"),
    DELIVERY_CHALLAN("CHL", "Delivery Challan"),
    OTHERS("OTH", "Others");

    private final String code;
    private final String description;

    DocumentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DocumentType fromCode(String code) {

        if (code == null || code.isBlank()) {
            return null;
        }

        for (DocumentType value : values()) {
            if (value.code.equalsIgnoreCase(code.trim())) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                "Invalid DocumentType code: " + code
        );
    }
}