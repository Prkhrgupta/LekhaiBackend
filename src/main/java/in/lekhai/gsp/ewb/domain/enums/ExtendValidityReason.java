package in.lekhai.gsp.ewb.domain.enums;

public enum ExtendValidityReason {
    NATURAL_CALAMITY("NATURAL_CALAMITY", 1),
    LAW_AND_ORDER("LAW_AND_ORDER", 2),
    TRANSSHIPMENT("TRANSSHIPMENT", 4),
    ACCIDENT("ACCIDENT", 5),
    OTHERS("OTHERS", 99);

    private final String reason;
    private final Integer reasonCode;

    ExtendValidityReason(String reason, Integer reasonCode) {
        this.reason = reason;
        this.reasonCode = reasonCode;
    }

    public Integer getReasonCode() {
        return this.reasonCode;
    }
}
