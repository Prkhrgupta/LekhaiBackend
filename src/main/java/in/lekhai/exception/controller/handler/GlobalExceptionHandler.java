package in.lekhai.exception.controller.handler;

import in.lekhai.common.Result;
import in.lekhai.exception.controller.exception.CategoryDoesNotExistException;
import in.lekhai.exception.controller.exception.RoleForCategoryDoesNotExistException;
import in.lekhai.exception.controller.exception.UsernameAlreadyExistException;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(PSQLException.class)
    public ResponseEntity<Result<?>> handlePSQLException(PSQLException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(Result.error(exception.getMessage()));
    }

    @ExceptionHandler(CategoryDoesNotExistException.class)
    public ResponseEntity<Result<?>> handleCategoryDoesNotExistException(CategoryDoesNotExistException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(Result.error(exception.getMessage()));
    }

    @ExceptionHandler(RoleForCategoryDoesNotExistException.class)
    public ResponseEntity<Result<?>> handleRoleDoesNotExistException(RoleForCategoryDoesNotExistException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(Result.error(exception.getMessage()));
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public ResponseEntity<Result<?>> handleUsernameAlreadyExistException(UsernameAlreadyExistException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(Result.error(exception.getLocalizedMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleAllException(Exception exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(Result.error(exception.getLocalizedMessage()));
    }
}