package in.lekhai.error.controller.voucher.exception;

import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

public class SeriesNotFoundException extends LekhaiClientException {
    public SeriesNotFoundException(Long id) {
        super(String.format("Voucher series id=[%s] not found", id), HttpStatus.NOT_FOUND);
    }
}
