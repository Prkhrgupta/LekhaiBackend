# executors — details

## Files

| File | Role |
|---|---|
| `lekhai/ContextDecorator.java` | `TaskDecorator` impl: wraps each submitted task so `ShopContext`'s current shop code is captured before handoff and restored inside the worker thread. |

## Behavioral Notes

- Spring Boot's default `@Async` executor does NOT carry `ShopContext` (a ThreadLocal). Any async path that reads/writes shop-scoped rows must run through an executor decorated with `ContextDecorator`, otherwise RLS scoping is lost and queries return empty/fail.
- The decorator should defensively `clear()` the child thread's `ShopContext` afterward to avoid leakage between pooled tasks.