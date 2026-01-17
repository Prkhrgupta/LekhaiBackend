package in.lekhai.error.controller.shop.exception;

import in.lekhai.error.controller.LekhaiException;

public class InvalidTenantTypeException extends LekhaiException {
    public InvalidTenantTypeException(String classType) {
        super(String.format("Invalid shopCode type: %s", classType));
    }
}
