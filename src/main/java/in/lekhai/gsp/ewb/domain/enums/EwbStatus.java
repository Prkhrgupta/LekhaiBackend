package in.lekhai.gsp.ewb.domain.enums;

public enum EwbStatus {

    ACT(
            "ACT",
            "Active",
            in.lekhai.contract.model.EwbStatus.ACTIVE
    ),

    CNL(
            "CNL",
            "Cancelled",
            in.lekhai.contract.model.EwbStatus.REJECTED
    ),

    DIS(
            "DIS",
            "Discarded",
            in.lekhai.contract.model.EwbStatus.REJECTED
    );

    private final String code;

    private final String description;

    private final in.lekhai.contract.model.EwbStatus ewbSummaryStatus;

    EwbStatus(String code,
              String description,
              in.lekhai.contract.model.EwbStatus ewbSummaryStatus) {
        this.code = code;
        this.description = description;
        this.ewbSummaryStatus = ewbSummaryStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public in.lekhai.contract.model.EwbStatus getEwbSummaryStatus() {
        return ewbSummaryStatus;
    }

    public static EwbStatus fromCode(String code) {

        if (code == null || code.isBlank()) {
            return null;
        }

        for (EwbStatus value : values()) {
            if (value.code.equalsIgnoreCase(code.trim())) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                "Invalid EwbStatus code: " + code
        );
    }
}