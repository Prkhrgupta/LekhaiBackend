package in.lekhai.error.controller.voucher.exception;

import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

public class SeriesValidationException extends LekhaiClientException {
    public SeriesValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
