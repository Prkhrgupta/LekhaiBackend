# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

`AGENT.md` is the human-facing onboarding doc and covers the tech stack, package map, and the full command list. This file focuses on the cross-cutting architecture you can only understand by reading several files together. Read both.

> **Building a new master/data-entry screen?** Read `docs/SCREEN_DEVELOPMENT_PLAYBOOK.md`. It is the end-to-end recipe for adding a screen across all three repos (Apispec, Backend, Frontend), derived from the Commodity (`inventory_master`) reference implementation. §4 covers the backend steps.

## Commands

```bash
./gradlew bootRun                            # run locally (port 8080)
./gradlew build -x test                      # build, skip tests
./gradlew test                               # run all tests
./gradlew test --tests in.lekhai.authentication.service.JwtTokenServiceTest   # single test class
./gradlew test --tests '*JwtTokenServiceTest.someMethod'                      # single method
./gradlew flywayMigrate -Duser.timezone=UTC  # apply migrations (always pass UTC)
./gradlew jibDockerBuild                     # build image into local docker daemon
docker compose up -d                         # local postgres on :5432 (db `lekhai`)
```

Java 21, Spring Boot 3.4.1, Gradle. App config in `src/main/resources/application{,-staging,-prod}.yaml`; sensitive values come from env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PUBLIC_KEY`, `PRIVATE_KEY`, `TAX_PRO_*`, etc.).

## Spec-first contracts (do not hand-write endpoints)

REST API interfaces and request/response DTOs are **not defined in this repo**. They are generated from the external `lekhai-apispec` module and consumed as the Maven dependency `in.lekhai:lekhaiapispec` (see `build.gradle`). Controllers `implement` interfaces from `in.lekhai.contract.api` and use models from `in.lekhai.contract.model`.

To change an endpoint signature or a request/response DTO: edit the spec in `lekhai-apispec`, publish it to Maven local, bump the version in `build.gradle`, then implement the regenerated interface here. Do **not** add custom `@RequestMapping` controllers or DTO classes for the public API in this repo.

## Multi-tenancy is enforced at the database, not in queries

Every shop's data is isolated by Postgres **Row-Level Security**, keyed on a `shop_code` column. Application queries do **not** filter by shop — the database does. The chain:

1. `ShopContextFilter` (servlet filter, runs after JWT auth) reads `shopCode` from the JWT claims and stores it in `ShopContext`, a `ThreadLocal`. Cleared in a `finally` block per request.
2. `ShopContextTransactionManager` (a `DataSourceTransactionManager` subclass) runs `SET LOCAL app.shop_code` / `SET LOCAL app.bypass_rls` on the JDBC connection in `doBegin`, and `RESET` in `doCleanupAfterCompletion`. **Because the value is set per-transaction, RLS-protected reads/writes must run inside a transaction.** Annotate service methods with `@ShopContextTransactional` (an alias for `@Transactional("shop-context-transaction-manager")`) — not plain `@Transactional`.
3. RLS policies (defined via the `create_shop_isolation_policy` procedures in `V6__create_rls_procedures.sql`, applied per-table in later migrations) filter on `shop_code = current_setting('app.shop_code')`. Rows with `shop_code = 0` are global/shared and visible to everyone.
4. On the write side, `ShopAwareEntityCallback` (a Spring Data `BeforeConvertCallback`) auto-stamps `shop_code` from `ShopContext` onto any entity extending `ShopAwareEntity`, so you never set it manually.

`SUPER_ADMIN` (shop_code `0`, see `SuperAdminConstants`) sets `app.bypass_rls = true` to see across all shops. A null `ShopContext` at transaction start is a hard error (`InvalidShopCodeException`).

**Implication for new features:** new tenant-scoped tables need an RLS policy in their migration; new entities should extend `ShopAwareEntity`; new service methods that touch the DB need `@ShopContextTransactional`.

## Layering & security conventions

- Standard Spring layering: Controller (implements spec interface) → Service (`@ShopContextTransactional`, business logic) → Repository (Spring Data JDBC, `CrudRepository`). Persistence is **Spring Data JDBC**, not JPA/Hibernate — no lazy loading, no entity graph; aggregates are loaded explicitly.
- Authorization is method/class-level via `@PreAuthorize` with constants in `SecurityExpressions` (`IS_SUPER_ADMIN`, `IS_SHOP_OWNER`, `NOT_SUPER_ADMIN`). Roles are the `Roles` enum (`SUPER_ADMIN`, `SHOP_OWNER`, `ADMIN`, `USER`), carried in the JWT `scope` claim. JWTs are RS256, validated against the keys in `application.yaml` (`rsa.*`).
- Fine-grained, per-screen feature access uses a permission **bitmask** (`PermissionBitCalculator`, `List<Long>` where each bit is a feature) rather than roles. The menu/feature hierarchy is assembled in `core/service/menu` and `core/service/feature`. For how the sidebar menu is configured, the relevant `/api/feature/*` and `/api/category/*` endpoints, and how to add/debug menu items, see `docs/menu-configuration.md`.
- Errors: throw subclasses of `LekhaiException` / `LekhaiClientException` (domain-specific exceptions live under `error/controller/**/exception/`); `GlobalExceptionHandler` (`@ControllerAdvice`) maps them to HTTP responses. Don't build `ResponseEntity` error bodies by hand.

## GSP / e-Way Bill integration (`gsp/ewb`)

The EWB module follows a ports-and-adapters layout: `domain/` (entities, enums, `model/`, and `port/EwbProvider`) is provider-agnostic; `infrastructure/taxpro/` is the concrete TaxPro adapter (WebClient clients, DTOs, mapper). The active provider is selected by config (`provider.ewb: tax-pro`). To support another GSP, implement `EwbProvider` in a new `infrastructure/` adapter rather than editing domain code. TaxPro auth tokens are short-lived (~15 min); credential/token handling is in `infrastructure/taxpro/service` and `GspCredentialService`.

## Database migrations

Flyway scripts in `src/main/resources/db/migration/`, named `V<n>__<description>.sql`, plain PostgreSQL. **Never edit a migration that has already been applied** — add a new versioned file. App startup has Flyway disabled (`spring.flyway.enabled: false`); migrations run via the `flywayMigrate` Gradle task (config in `flyway.conf`). Spring Batch tables are created by migration `V11`, not auto-initialized (`spring.batch.initialize-schema: never`).

## Other

- CSV import uses a factory/strategy pattern: `CsvUploadFactory` dispatches by `CsvUploadTypes` to an implementation under `csv/upload/service/implementations/` (OpenCSV). Excel export uses Apache POI via the `common/excel/` helpers (`ExcelExporter`, `@ExcelColumn`).
- Structured JSON logging (Logstash encoder → Loki) plus Micrometer/Brave tracing; `logback-spring.xml`. `ShopContext` is propagated to async executor threads via `ContextDecorator` (`executors/`).
- Seed data (`states.csv`, `account_groups.csv` under `resources/seeds/`) is loaded by seeders in `core/account_master/seeders/`.
