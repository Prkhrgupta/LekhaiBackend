package in.lekhai.except.handler;

import in.lekhai.authentication.exception.UserDoesNotExistException;
import in.lekhai.common.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<?>> handleUserNotFoundException(UserDoesNotExistException exception) {
        return ResponseEntity.badRequest().body(Result.error(exception.getMessage()));
    }

}
