# error

**How business-rule failures and system errors reach the user** — the single, consistent error vocabulary of the whole product. When the books can't accept something (a voucher that doesn't balance, a ledger that doesn't exist, a duplicate master name), this module turns it into a structured, HTTP-correct response the UI can show cleanly instead of a raw stack trace.

## Business Goal

Accounting software must fail loudly and clearly: if a posting is invalid or a party can't be found, the person at the screen needs to know *why*, and the platform needs one predictable error shape. This module owns that contract:

- **Base failure kinds** — `LekhaiException` (a server-side failure, 500-level) and `LekhaiClientException` (the caller did something wrong — carries the 4xx status). All accounting/business errors descend from these.
- **Domain-specific exceptions** — precise named exceptions per business area (ledger/account not found, category already exists, commodity/item-category/stock-item validation, user/role problems, invalid shop). They live *grouped by owning domain* under `error/controller/<domain>/exception/`, not sprinkled through feature packages.
- **One global handler** — a `@RestControllerAdvice` maps every exception type to a standard `Result<T>` error envelope: domain exceptions, Spring validation (field-level messages), duplicate keys, database integrity violations, and access-denied. Controllers never build error responses by hand.

## Public API

- `controller/GlobalExceptionHandler` — the single `@RestControllerAdvice` that turns every failure into a `Result<T>` error response.
- `controller/LekhaiException` — base runtime exception; a server error (500) by default.
- `controller/LekhaiClientException` — runtime exception carrying an HTTP status, mapped to that status (typically 4xx).
- `controller/<domain>/exception/*` — the precise, named domain exceptions (account, category, commodity, feature, item category/factory, state of ledger-settings, role, sale/purchase ledger setting, shop, stock item, user).

## Dependencies

- `common/` (`Result<T>` envelope).

## How to Extend

To surface a new business-rule failure:

1. Create `<Name>Exception` extending `LekhaiException` or `LekhaiClientException`, placed at `error/controller/<domain>/exception/` (the domain is the business area, not the source module).
2. Register a handler in `GlobalExceptionHandler` — or let the base handler map it.
3. Throw it from the service/controller layer where the rule is enforced.
4. Name exceptions precisely (`XNotFound`, `XAlreadyExist`, `XDoesNotExist`) — don't reuse one exception for many 4xx cases, and don't add a domain exception for cases Spring's `@Valid` already reports.

## Deep Dive

> Read `DETAILS.md` only when: adding/mapping exception types, changing HTTP status semantics, or touching `Result<T>` error shapes.