package in.lekhai.error.controller.uom.exception;

import in.lekhai.error.controller.LekhaiClientException;
import org.springframework.http.HttpStatus;

public class UomInUseException extends LekhaiClientException {
    public UomInUseException(Long id) {
        super("This unit cannot be deleted because stock items are using it.", HttpStatus.CONFLICT);
    }

    public UomInUseException(Long id, String reason) {
        super(reason, HttpStatus.CONFLICT);
    }
}
