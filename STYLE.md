# Lekhai Backend Style Guide

Mandatory for all code written in this repo. These rules keep the codebase consistent and predictable for both humans and AI agents.

## Naming

- **Classes/Interfaces:** `PascalCase`. Entities end without a suffix except contract-groups (e.g., `Ledger`, `Shops`). Interface-based services for patterns (e.g., `CsvUploadService`).
- **Methods:** `camelCase`, strong verbs (`create`, `validate`, `fetchById`).
- **Variables/fields:** `camelCase`, descriptive, no single letters except loop indexes.
- **Constants:** `UPPER_SNAKE_CASE`.
- **Packages:** lowercase, follow the existing domain groups (`controller`, `service`, `repository`, `domain`, `dto`, `utils`, `config`).
- **Exceptions:** `*Exception` suffix in the matching `error/controller/<domain>/exception/` package.

## Line & Size Limits

- **Methods:** max **25 lines** of body. If longer, split into private helpers.
- **Constructors / dependency injection:** prefer single constructor injection; keep constructor arg lists readable.
- **Files:** keep under ~300 lines; split large services into cohesive classes.

## Comments

- **No comments on obvious code.** `// increments counter` is noise.
- **Use Javadoc** (`/** */`) only on public API surface where behavior isn't obvious from the signature.
- **Explain WHY, not WHAT.** If logic is non-trivial (business rule, workaround, performance decision), a brief `// why` comment is required.
- **Never commit commented-out code.** Delete it.

## Formatting

- **Indentation:** 4 spaces. No tabs.
- **Braces:** same-line opening (`K&R`). Always use braces, even for single-line blocks.
- **Blank lines:** one blank line between methods; group related statements logically.
- **Line length:** target ≤ 120 chars.
- **Imports:** no wildcard imports. Group: `java` / `javax`, then `org.springframework`, then project (`in.lekhai.contract.*`, `in.lekhai.*`), then others, each group alphabetical.

## Architecture & Error Handling

- **Layers:** Controller → Service → Repository. Controllers stay thin (validation + delegation only); business logic lives in services.
- **Errors:** throw `LekhaiException`, `LekhaiClientException`, or a domain subclass from `error/`. Never throw raw `RuntimeException` or swallow exceptions.
- **Shop scoping:** any entity holding shop data extends `common.domain.ShopAwareEntity`. Never set `shop_code` manually in service code — use `setShopCodeIfNull`.
- **Contracts:** never hand-write REST DTOs or endpoint signatures that exist in `in.lekhai.contract.*`.
- **Records for value objects/DTOs:** use Java `record` where the type is immutable.
- **`Result<T>` envelopes:** never build success/error responses ad-hoc in controllers; let the global handler produce them.

## Validation

- Validate request bodies with Jakarta `@Valid` + constraints (`@NotNull`, `@Size`, `@Pattern`).
- Partial updates: null-check before applying; don't overwrite with nulls.

## Testing

- Name tests `<ClassUnderTest>Test` / `<ClassUnderTest>IntegrationTest`, live in a mirrored package under `src/test/java/`.
- Use AssertJ fluent assertions.
- Read tests before changing behavior — they define intended contract.