package in.lekhai.exception.controller.exception;

import in.lekhai.core.model.enums.Roles;

public class RoleForCategoryDoesNotExistException extends RuntimeException {
    public RoleForCategoryDoesNotExistException(Roles role, Long category) {
        super(String.format("%s roles does not exist for category %s", role, category));
    }
}
