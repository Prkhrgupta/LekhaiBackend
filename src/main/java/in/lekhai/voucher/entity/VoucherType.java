package in.lekhai.voucher.entity;

public enum VoucherType {

    PAYMENT("PY"),
    RECEIPT("RC"),
    CONTRA("CNT"),
    JOURNAL("JN"),
    PURCHASE("PR"),
    SALES("SV"),
    CREDIT_NOTE("CN"),
    DEBIT_NOTE("DN");

    private final String prefix;

    VoucherType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}