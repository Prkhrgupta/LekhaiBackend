# error — details

## Files

| File | Role |
|---|---|
| `controller/GlobalExceptionHandler.java` | `@RestControllerAdvice`. Handlers: `LekhaiException`, `LekhaiClientException`, validation (`MethodArgumentNotValidException`), `DataIntegrityViolationException`/PSQL, `DuplicateKeyException`, `AccessDeniedException`/`AuthorizationDeniedException`. Returns `Result<T>` envelope. |
| `controller/LekhaiException.java` | Base runtime exception. Maps to server error by default. |
| `controller/LekhaiClientException.java` | Runtime exception with `HttpStatus`; maps to that status. |
| `<domain>/exception/*.java` | One-off domain exceptions. Existing domains: `account`, `category`, `commodity`, `feature`, `itemcategory`, `itemfactory`, `purchaseledgersetting`, `role`, `saleledgersetting`, `shop` (4), `stockitem`, `user` (2). |

## Behavioral Notes

- Package layout mirrors the owning domain (`error/controller/<domain>/exception/`), NOT the source module. Follow it when creating new exceptions; do not scatter exceptions in feature packages.
- Handler ordering matters: more specific exceptions get dedicated handlers; generic PSQL/duplicate-key handling is the fallback.
- Keep exception names precise (`XAlreadyExist` vs `XDoesNotExist` vs `XNotFound`); do not reuse one exception for multiple 4xx cases.
- User-visible failures extend `LekhaiClientException` and must always pass an explicit status (`HttpStatus`) — the single-arg constructor leaves `statusCode` null and NPEs in the handler. Keep the message toast-safe (no ids or `shopCode`); log internals with `log.warn` at the throw site.
- Validation binding errors surface as field-level messages; don't add domain exceptions for cases Spring already handles (e.g., `@Valid` failures).