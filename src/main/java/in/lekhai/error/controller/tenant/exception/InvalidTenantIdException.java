package in.lekhai.error.controller.tenant.exception;

import in.lekhai.error.controller.LekhaiException;

public class InvalidTenantIdException extends LekhaiException {
    public InvalidTenantIdException() {
        super("Null/Empty tenant id");
    }
}
