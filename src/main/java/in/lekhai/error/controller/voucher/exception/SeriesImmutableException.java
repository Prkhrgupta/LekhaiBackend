package in.lekhai.error.controller.voucher.exception;

import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

public class SeriesImmutableException extends LekhaiClientException {
    public SeriesImmutableException(Long id) {
        super(String.format(
                "Voucher series id=[%s] cannot change prefix/suffix after vouchers exist",
                id),
                HttpStatus.CONFLICT);
    }
}
