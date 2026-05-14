package in.lekhai.gsp.ewb.domain.enums;

public enum TransactionType {

    REGULAR(1, "Regular"),
    BILL_TO_SHIP_TO(2, "Bill To - Ship To"),
    BILL_FROM_DISPATCH_FROM(3, "Bill From - Dispatch From"),
    COMBINATION_OF_2_AND_3(4, "Combination of 2 and 3");

    private final Integer code;
    private final String description;

    TransactionType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static TransactionType fromCode(Integer code) {

        if (code == null) {
            return null;
        }

        for (TransactionType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                "Invalid TransactionType code: " + code
        );
    }
}