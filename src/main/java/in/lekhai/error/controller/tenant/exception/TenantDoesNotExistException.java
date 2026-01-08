package in.lekhai.error.controller.tenant.exception;

import in.lekhai.core.enums.Roles;
import in.lekhai.error.controller.LekhaiException;

public class TenantDoesNotExistException extends LekhaiException {
    public TenantDoesNotExistException(String uuid, Roles role) {
      super(String.format("Invalid request: No shopCode exists with UUID [%s] for roleId [%s]", uuid, role));
    }
}
