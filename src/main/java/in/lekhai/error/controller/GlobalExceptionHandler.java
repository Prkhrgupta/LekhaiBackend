package in.lekhai.error.controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import in.lekhai.common.Result;
import in.lekhai.contract.model.Error;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;

import org.postgresql.util.PSQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(LekhaiException.class)
    public ResponseEntity<Result<?>> handleLekhaiException(LekhaiException exception) {
        log.error("{}", exception.getMessage(), exception);
        return ResponseEntity.badRequest().body(Result.error(exception.getLocalizedMessage()));
    }

    @ExceptionHandler(SeriesFyMissingException.class)
    public ResponseEntity<Error> handleSeriesFyMissing(SeriesFyMissingException exception) {
        // Why bespoke: the contract needs error=SERIES_FY_MISSING with data,
        // which the base handler cannot produce from status code alone.
        return ResponseEntity.unprocessableEntity().body(
                new Error()
                        .success(Boolean.FALSE)
                        .error("SERIES_FY_MISSING")
                        .message(exception.getMessage())
                        .data(Map.of(
                                "voucher_type", exception.getVoucherType(),
                                "financial_year", exception.getFinancialYear()))
                        .timestamp(OffsetDateTime.now(ZoneOffset.UTC)));
    }

    @ExceptionHandler(LekhaiClientException.class)
    public ResponseEntity<Error> handleLekhaiClientException(LekhaiClientException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(
                new Error()
                        .success(Boolean.FALSE)
                        .error(exception.getStatusCode().toString())
                        .message(exception.getMessage())
        );
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
