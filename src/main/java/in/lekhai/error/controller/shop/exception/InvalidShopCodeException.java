package in.lekhai.error.controller.shop.exception;

import in.lekhai.error.controller.LekhaiException;

public class InvalidShopCodeException extends LekhaiException {
    public InvalidShopCodeException() {
        super("Invalid request: Null/Empty tenant id");
    }
}
