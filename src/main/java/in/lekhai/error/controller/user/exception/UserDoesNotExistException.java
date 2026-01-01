package in.lekhai.error.controller.user.exception;

import in.lekhai.core.enums.Roles;
import in.lekhai.error.controller.LekhaiException;

public class UserDoesNotExistException extends LekhaiException {
    public UserDoesNotExistException(String uuid, Roles role) {
        super(String.format("No user with adminUuid [%s] and role [%s] exists", uuid, role));
    }
}
