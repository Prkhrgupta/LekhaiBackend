package in.lekhai.except.handler;

import in.lekhai.common.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;

@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<?>> handleUsernameNotFoundException(UsernameNotFoundException exception) {
        return ResponseEntity.badRequest().body(Result.error(exception.getMessage()));
    }

}
