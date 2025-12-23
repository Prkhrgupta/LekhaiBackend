package in.lekhai.error.controller.tenant.exception;

import in.lekhai.error.controller.LekhaiException;

public class InvalidTenantTypeException extends LekhaiException {
    public InvalidTenantTypeException(String classType) {
        super(String.format("Invalid tenant type: %s", classType));
    }
}
