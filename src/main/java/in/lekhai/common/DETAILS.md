# common — details

## Files

| File | Role |
|---|---|
| `Result.java` | `record Result<T>(success, message, data, error, timestamp)`. Factories: `success(data)`, `success(message,data)`, `success(message)`, `error(message)`, `error(message,error)`. `@JsonInclude(NON_NULL)`. |
| `domain/ShopAwareEntity.java` | Abstract. Fields: `Integer shopCode`, `@CreatedDate Instant createdAt`, `@LastModifiedDate Instant updatedAt`. `setShopCodeIfNull(Integer)` only sets when currently null. |
| `ShopAwareEntityCallback.java` | `BeforeConvertCallback<ShopAwareEntity>` — calls `setShopCodeIfNull(ShopContext.getShopCode())` on save so RLS scoping is consistent. |
| `excel/ExcelExporter.java` | Generic POI workbook writer driven by `List<ExcelColumn>`. |
| `excel/ExcelColumn.java` | Column definition (`header`, `mapping`/extractor). |
| `excel/WorkbookCustomizer.java` | Hook to tweak the built workbook before serialization. |
| `config/CacheConfig.java` | Caffeine cache manager + named caches (e.g., state/account-group lookups). |
| `AccountEntryType.java` | Enum of account entry directions (debit/credit side). |
| `JwtConstants.java` | JWT claim name constants. |
| `SuperAdminConstants.java` | Fixed super-admin role/feature constants used by provisioning. |
| `util/FinancialYearDateUtil.java` | Financial-year start/end date computation. |

## Behavioral Notes

- `ShopAwareEntityCallback` runs on every repository save of a `ShopAwareEntity`. If `ShopContext` throws (`InvalidShopCodeException`) when no code is set, saves fail — this is intentional RLS enforcement.
- Exports are memory-bound; assume small-medium datasets (no streaming).
- Never bypass `ShopContext` when persisting shop entities; the callback + DB RLS are the only guards.