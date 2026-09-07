# shop — details

## Files & Roles

| File | Role |
|---|---|
| `context/model/ShopContext.java` | `ThreadLocal<Integer>`. `setShopCode(null)` throws `InvalidShopCodeException`; `clear()` on request end. |
| `context/filter/ShopContextFilter.java` | Once-per-request: reads shop from auth/request, calls `ShopContext.setShopCode`, clears in `finally`. |
| `context/transaction/manager/ShopContextTransactionManager.java` | Extends `DataSourceTransactionManager`; on connection acquire, sets RLS session var (`SET app.shop_code = ?` or similar) tied to `ShopContext`. |
| `context/transaction/manager/annotation/ShopContextTransactional.java` | `@Transactional` meta-annotation; points at the custom transaction manager. |
| `context/transaction/manager/config/ShopContextTransactionManagerConfig.java` | Declares the custom manager as the primary `PlatformTransactionManager`. |

## Behavioral Notes

- Enforcing multi-tenancy is a stack: `ShopContextFilter` (HTTP) → `ShopContext` (ThreadLocal) → `ShopContextTransactionManager` (DB connection RLS) → `ShopAwareEntityCallback` (row `shop_code` on insert, in `common/`).
- Async tasks lose ThreadLocal automatically; `ContextDecorator` in `executors/` restores it. Use it for any `@Async` path that queries shop data.
- Queries outside a thread carrying `ShopContext` that touch shop-scoped tables will not see rows (RLS) and inserts may fail (`InvalidShopCodeException` / RLS violation). This is by design.
- `@ShopContextTransactional` must be used instead of plain `@Transactional` when the work depends on RLS scoping.