# error

Central exception handling. All domain exceptions and the global `@RestControllerAdvice` live here.

## Public API

- `GlobalExceptionHandler` — `@RestControllerAdvice` mapping each exception type to a `Result<T>` error response.
- `LekhaiException` — base runtime exception (500-level).
- `LekhaiClientException` — carries an HTTP status code (4xx).
- Domain exception subclasses under `controller/<domain>/exception/` (e.g., `shop/exception/InvalidShopCodeException`, `category/exception/CategoryAlreadyExistException`).

## Dependencies

- `common/` (`Result<T>`)

## How to Extend

Add a new domain exception:
1. Create `<Name>Exception` extending `LekhaiException` or `LekhaiClientException` in `error/controller/<domain>/exception/`.
2. Register a handler method in `GlobalExceptionHandler` (or map it via the base handler).
3. Throw it from services/controllers.

## Deep Dive

> Read `DETAILS.md` only when: adding/mapping exception types, changing HTTP status semantics, or touching `Result<T>` error shapes.