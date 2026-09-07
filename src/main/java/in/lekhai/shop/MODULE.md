# shop

**Multi-tenancy / firm isolation** — the guarantee that one shop's books can never leak into another's. Lekhai hosts many firms on one platform; each shop's ledgers, vouchers, inventory and GST records are its own, exactly as if it kept a separate physical register.

## Business Goal

Lekhai is cloud accounting for many independent businesses at once. A MSME owner trusts that their figures are visible to no one else, and that another firm's data can't corrupt theirs. This module enforces that trust at every layer, not just in the UI:

- **Identify the firm** — each authenticated request carries which shop it belongs to (`ShopContext`), established and cleared per request so no stale firm leaks across requests.
- **Isolate the books in the database** — every transaction binds that shop's code onto the database connection via **Row-Level Security** (RLS), so a query physically *cannot* see or write another firm's rows, even if code were written incorrectly. Inserts against shop-scoped tables require an active shop; without one the operation is rejected (`InvalidShopCodeException`).
- **Scope transactions** — a dedicated `@ShopContextTransactional` marks work that must run inside the firm's RLS boundary (the entire `voucher/` posting engine, ledger reports, imports, etc.).

The isolation is a deliberate stack — HTTP filter → thread-local context → connection-level RLS → default shop-code on insert — so "correct and tenant-isolated books" is enforced by the database itself, the strongest guarantee a financial product can give.

## Public API

- `context/model/ShopContext` — the thread-local holder of the active shop code; `set`/`get`/`clear`. Setting an invalid code throws `InvalidShopCodeException`.
- `context/filter/ShopContextFilter` — once per request: reads the firm from the authenticated request, sets `ShopContext`, and always clears it afterwards.
- `context/transaction/manager/ShopContextTransactionManager` — the DB transaction manager that applies the shop code as RLS on each connection.
- `context/transaction/manager/annotation/ShopContextTransactional` — the annotation that runs a block inside a shop-scoped RLS transaction (replaces plain `@Transactional` for shop work).
- `context/transaction/manager/config/ShopContextTransactionManagerConfig` — wires the custom manager in as the primary transaction manager.

## Dependencies

- `authentication/` (the request's firm comes from the authenticated principal), `error/` (`InvalidShopCodeException`), `common/` (row shop-code injection on insert, `ShopAwareEntityCallback`).

## How to Extend

- Make any transactional block firm-scoped: annotate with `@ShopContextTransactional` instead of `@Transactional` whenever the work touches shop-scoped tables.
- Any `@Async` job that reads/writes shop data must restore the context with the `ContextDecorator` from `executors/` — ThreadLocal does not survive across threads on its own.

## Deep Dive

> Read `DETAILS.md` only when: changing RLS binding, transaction-manager behaviour, or filter ordering.