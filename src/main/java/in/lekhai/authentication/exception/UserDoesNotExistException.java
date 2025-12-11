package in.lekhai.authentication.exception;

import org.springframework.security.core.AuthenticationException;

public class UserDoesNotExistException extends AuthenticationException {
    public UserDoesNotExistException(String username) {
        super(String.format("User %s not found", username));
    }
}
