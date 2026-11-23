package in.lekhai.error.controller.feature.exception;

import in.lekhai.error.controller.LekhaiException;

public class InvalidSuperAdminRouteException extends LekhaiException {
    public InvalidSuperAdminRouteException(String route, String reason) {
        super(String.format("Invalid super-admin route [%s]: %s", route, reason));
    }
}
