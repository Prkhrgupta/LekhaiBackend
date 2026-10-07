package in.lekhai.error.controller.voucher.exception;

import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

public class SeriesFyMissingException extends LekhaiClientException {
    private final String voucherType;
    private final String financialYear;

    public SeriesFyMissingException(String voucherType, String financialYear) {
        super(String.format(
                "No voucher series configured for voucher_type %s in financial year %s",
                voucherType, financialYear),
                HttpStatus.UNPROCESSABLE_ENTITY);
        this.voucherType = voucherType;
        this.financialYear = financialYear;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public String getFinancialYear() {
        return financialYear;
    }
}
