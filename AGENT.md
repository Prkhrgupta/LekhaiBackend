# Lekhai Backend Agent Guide

This file is the entry point for AI agents working on **LekhaiBackend**. It is thin on purpose — module-specific knowledge lives in each module's own docs, loaded only when needed.

## 🚀 Overview

`LekhaiBackend` is the Java backend for the Lekhai B2B application, which lets shops and sellers in India manage accounts, ledgers, inventory, vouchers, and invoicing.

* **Language/Runtime:** Java 21 (Eclipse Temurin)
* **Framework:** Spring Boot 3.4.1
* **Build Tool:** Gradle

## 📚 Documentation Map (READ THIS FIRST)

Read docs on a **need-to-know basis**. Do NOT load everything upfront.

| What | Where | When to read |
|---|---|---|
| Code style & formatting rules | `STYLE.md` | **Always** — before writing ANY code |
| Build & commands | this file, §Commands | Always |
| Architecture principles | this file, §Principles | Always |
| Module interface card | `<module>/MODULE.md` | When touching that module |
| Module deep dive | `<module>/DETAILS.md` | Only when modifying internals or confused |

### Module doc rule

Each package under `src/main/java/in/lekhai/` is a module with a `MODULE.md` (interface summary) and a `DETAILS.md` (implementation).

1. Read the module's `MODULE.md` **first**.
2. Read its `DETAILS.md` **only** when you cannot understand something from `MODULE.md`, or you must modify the module's internal implementation.
3. When extending a module, prefer the "How to Extend" section of its `MODULE.md`.
4. Prefer `@file:MODULE.md`-style references: name the module file explicitly in your reasoning.

## 🗂 Module Index

All modules live under `src/main/java/in/lekhai/`.

| Module | Purpose | Files |
|---|---|---|
| `common/` | Base entity, `Result<T>` envelope, Excel helpers, constants, cache config | 11 |
| `error/` | Exception hierarchy + `GlobalExceptionHandler` | 22 |
| `authentication/` | JWT, security config, login, user accounts | 15 |
| `shop/` | Multi-tenancy: `ShopContext` ThreadLocal, RLS transaction manager | 5 |
| `executors/` | Async `ContextDecorator` (ThreadLocal propagation) | 1 |
| `category/` | Transporters + E-way bill scheduling | 5 |
| `accountbooks/` | Purchase/sales ledger report slice | 2 |
| `voucher/` | Voucher processing (payment/receipt/contra/journal) + posting | 19 |
| `gsp/` | GST Suvidha Provider (TaxPro EWB + GSTIN) — hexagonal | 41 |
| `csv/` | Batch CSV uploads (transport/area/broker/ledger) | 13 |
| `core/domain/` | Core domain entities (admin, category, feature, role, shop, user) | 10 |
| `core/repository/` | Repository interfaces for core domain | 11 |
| `core/dto/` | Internal DTOs (only where no generated contract exists) | 17 |
| `core/controller/` | Top-level controllers (admin, category, feature, menu, shop, superadmin) | 6 |
| `core/service/` | Core services (admin, category, feature, menu, shop, superadmin) | 9 |
| `core/account_master/` | Account masters (ledger, broker, area, transport, state, account groups) | 43 |
| `core/inventory_master/` | Inventory masters (commodity, item category, factory, stock item) | 16 |

## 🔑 Architecture Principles

1. **Contracts are never hand-written.** REST endpoints and request/response DTOs come from the generated `in.lekhai:lekhaiapispec` jar (`in.lekhai.contract.*`). Update `lekhai-apispec`, publish to Maven local, then implement the updated interface.
2. **Layered architecture.** Controller → Service → Repository (Spring Data JDBC). No JPA.
3. **Shop multi-tenancy.** All shop-scoped entities extend `ShopAwareEntity`; `shop_code` is enforced via `ShopContext` ThreadLocal + Row-Level Security.
4. **Migrations are append-only.** Never modify an applied Flyway migration; add a new `V<version>__<name>.sql`.
5. **SQL is raw.** Queries use `@Query` with PostgreSQL SQL; no query builder.
6. **Errors through the hierarchy.** Throw `LekhaiException` / `LekhaiClientException` and domain subclasses; never raw `RuntimeException`.

## ⚙️ External Integration Flow

1. API specs are changed in `lekhai-apispec`, generated into Java interfaces, published to Maven local.
2. Backend references them in `build.gradle` (`in.lekhai:lekhaiapispec:1.1.3`).
3. Controllers implement `in.lekhai.contract.api` interfaces and use `in.lekhai.contract.model` DTOs.

## 💻 Commands

Run from the repo root:

* **Clean:** `./gradlew clean`
* **Build (skip tests):** `./gradlew build -x test`
* **Tests:** `./gradlew test`
* **Run locally:** `./gradlew bootRun`
* **Flyway migrate:** `./gradlew flywayMigrate -Duser.timezone=UTC`
* **Docker image (local):** `./gradlew jibDockerBuild`
* **Docker push:** `./gradlew jib`

## 🚧 Cross-Cutting Rules

1. **UTC.** Always use UTC for DB operations and commands (e.g., `flywayMigrate -Duser.timezone=UTC`).
2. **Validation.** Use validation annotations on request bodies.
3. **Testing.** Tests live in `src/test/java/in/lekhai/`. Run via `./gradlew test`. TestContainers required for DB-dependent tests.
4. **Doc maintenance.** After changing a module's public API (adding/removing public classes or changing service contracts), update that module's `MODULE.md` and `DETAILS.md` as part of the same change.