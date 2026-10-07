package in.lekhai.error.controller.voucher.exception;

import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

public class DuplicateBillNumberException extends LekhaiClientException {
    public DuplicateBillNumberException(Long billNo) {
        super(String.format("Bill no [%s] already exists in this series", billNo),
                HttpStatus.CONFLICT);
    }
}
