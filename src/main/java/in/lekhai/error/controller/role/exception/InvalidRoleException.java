package in.lekhai.error.controller.role.exception;

import in.lekhai.error.controller.LekhaiException;

public class InvalidRoleException extends LekhaiException {
    public InvalidRoleException(String message) {
        super(message);
    }
}
