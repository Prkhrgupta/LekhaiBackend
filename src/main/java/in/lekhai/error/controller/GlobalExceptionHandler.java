package in.lekhai.error.controller;

import in.lekhai.common.Result;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
        Object[] detailMessageArguments = exception.getDetailMessageArguments();
        for(int i = 0; i < detailMessageArguments.length; i++) {
            System.out.println(detailMessageArguments[i].toString());
        }
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(Result.error(exception.getBody().getDetail()));
    }

    @ExceptionHandler(PSQLException.class)
    public ResponseEntity<Result<?>> handlePSQLException(PSQLException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(Result.error("[PSQLException] Internal Server Error"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<?>> handleDuplicateKeyExceptions(DuplicateKeyException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.internalServerError().body(Result.error("Duplication entry"));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Result<?>> handleAuthorizationException(AuthorizationDeniedException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Result.error("Unauthorized"));
    }
}
