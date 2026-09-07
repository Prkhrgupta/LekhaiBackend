# shop

Shop multi-tenancy enforcement: `ShopContext` (ThreadLocal), per-connection RLS transaction manager, and request filter.

## Public API

- `context/model/ShopContext` — static ThreadLocal of `Integer shopCode`; `set`, `get`, `clear`.
- `context/filter/ShopContextFilter` — servlet filter: sets `ShopContext` from the authenticated request.
- `context/transaction/manager/ShopContextTransactionManager` — `DataSourceTransactionManager` that binds `shop_code` (RLS) per connection.
- `context/transaction/manager/annotation/ShopContextTransactional` — meta-annotation to multi-tenant-scope a transaction.
- `context/transaction/manager/config/ShopContextTransactionManagerConfig` — wires the custom transaction manager as primary.

## Dependencies

- `error/` (`InvalidShopCodeException`), `authentication/`, `common/`

## How to Extend

- Make a transactional block shop-scoped: annotate with `@ShopContextTransactional` (replaces `@Transactional`).
- Existing ThreadLocal propagation to async: see `executors/`.

## Deep Dive

> Read `DETAILS.md` only when: changing RLS binding, transaction manager behavior, or filter ordering.