package in.lekhai.error.controller.shop.exception;

import in.lekhai.error.controller.LekhaiException;

public class InvalidShopTypeException extends LekhaiException {
    public InvalidShopTypeException(String classType) {
        super(String.format("Invalid shopCode type: %s", classType));
    }
}
