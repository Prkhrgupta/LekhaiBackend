package in.lekhai.error.controller.shop.exception;

import in.lekhai.core.enums.Roles;
import in.lekhai.error.controller.LekhaiException;

public class TenantDoesNotExistException extends LekhaiException {
    public TenantDoesNotExistException(String uuid) {
      super(String.format("Invalid request: No tenant exists with UUID [%s]", uuid));
    }
}
