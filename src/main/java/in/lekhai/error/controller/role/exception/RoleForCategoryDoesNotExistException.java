package in.lekhai.error.controller.role.exception;

import in.lekhai.core.enums.Roles;
import in.lekhai.error.controller.LekhaiException;

public class RoleForCategoryDoesNotExistException extends LekhaiException {
    public RoleForCategoryDoesNotExistException(Roles role, Long category) {
        super(String.format("[%s] roles does not exist for category [%s]", role, category));
    }
}
