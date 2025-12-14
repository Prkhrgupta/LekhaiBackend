package in.lekhai.error.controller.tenant.exception;

import in.lekhai.core.model.enums.Roles;
import in.lekhai.error.controller.LekhaiException;

public class TenantDoesNotExistException extends LekhaiException {
    public TenantDoesNotExistException(String uuid, Roles role) {
      super(String.format("Invalid request: No tenant exists with UUID [%s] for role [%s]", uuid, role));
    }
}
