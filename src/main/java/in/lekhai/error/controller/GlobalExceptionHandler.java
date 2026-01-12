package in.lekhai.error.controller;

import in.lekhai.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(LekhaiException.class)
    public ResponseEntity<Result<?>> handleLekhaiException(LekhaiException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(Result.error(exception.getLocalizedMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleInvalidArgumentException(MethodArgumentNotValidException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(Result.error(exception.getLocalizedMessage()));
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Result<?>> handlePSQLException(SQLException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(Result.error("[PSQLException] Internal Server Error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleAllException(Exception exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(Result.error("Internal Server Error"));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Result<?>> handleAuthorizationException(AuthorizationDeniedException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error("Unauthorized"));
    }
}
