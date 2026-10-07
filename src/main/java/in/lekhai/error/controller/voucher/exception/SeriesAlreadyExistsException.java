package in.lekhai.error.controller.voucher.exception;

import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

public class SeriesAlreadyExistsException extends LekhaiClientException {
    public SeriesAlreadyExistsException(String seriesCode, String voucherType, String financialYear) {
        super(String.format(
                "Voucher series %s already exists for voucher_type %s in financial year %s",
                seriesCode, voucherType, financialYear),
                HttpStatus.CONFLICT);
    }
}
