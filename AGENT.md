# Lekhai Backend Agent Guide

This file provides key information, architecture guidelines, and common commands for developer agents working on the **LekhaiBackend** codebase.

## 🚀 Overview

`LekhaiBackend` is the Java backend server for the Lekhai B2B application, which allows shops and sellers in India to manage their accounts, ledgers, inventory, vouchers, and invoicing.

* **Language/Runtime:** Java 21 (Eclipse Temurin)
* **Framework:** Spring Boot 3.4.1
* **Build Tool:** Gradle

---

## 🛠 Tech Stack & Dependencies

* **Web & Routing:** `spring-boot-starter-web`, `spring-boot-starter-webflux`
* **Database & Persistence:** PostgreSQL, `spring-boot-starter-jdbc`, `spring-boot-starter-data-jdbc`
* **Database Migrations:** Flyway (`org.flywaydb.flyway` version `10.21.0`)
* **Security & Auth:** OAuth2 Resource Server, JWT token-based auth (`spring-boot-starter-security`)
* **Batch Processing:** Spring Batch Core (`5.1.3`)
* **Integration Spec:** `in.lekhai:lekhaiapispec` (published from [lekhai-apispec](file:///Users/dream/Lekhai/lekhai-apispec))
* **Containerization:** Google Jib (`com.google.cloud.tools.jib` version `3.4.7`)
* **Caching:** Caffeine Cache (`com.github.ben-manes.caffeine:caffeine`)
* **Monitoring & Tracing:** Micrometer Tracing (Brave bridge), Logstash Logback encoder (for JSON structured logging to Grafana Loki)
* **File Handlers:** OpenCSV (`5.9`), Apache POI (`5.2.5`) for Excel exports/imports

---

## 📂 Project Structure

Key paths in `LekhaiBackend`:

* **Main Entry Point:** [LekhaiApplication.java](file:///Users/dream/Lekhai/LekhaiBackend/src/main/java/in/lekhai/LekhaiApplication.java)
* **Configuration & Resources:** [src/main/resources/](file:///Users/dream/Lekhai/LekhaiBackend/src/main/resources/)
  * [application.yaml](file:///Users/dream/Lekhai/LekhaiBackend/src/main/resources/application.yaml): Default/local config.
  * [db/migration/](file:///Users/dream/Lekhai/LekhaiBackend/src/main/resources/db/migration/): Flyway SQL migration scripts. Named using pattern `V<version>__<description>.sql`.
  * [keys/](file:///Users/dream/Lekhai/LekhaiBackend/src/main/resources/keys/): Public and private key files for OAuth2 JWT token signature validation.
  * [flyway.conf](file:///Users/dream/Lekhai/LekhaiBackend/src/main/resources/flyway.conf): Flyway connection and migration settings.
* **Source Code packages (`src/main/java/in/lekhai/`):**
  * `authentication/`: Security contexts, CORS configurations, JWT validation/decoding, token resource configuration.
  * `category/`: Product categories and classifications.
  * `common/`: Core generic utilities, logging filters, common helpers.
  * `core/`: Base classes and system setups.
  * `csv/`: OpenCSV parsing rules and mappings.
  * `error/`: Exception handling, custom error responses, and controller advices.
  * `executors/`: Thread pools and async runner configurations.
  * `gsp/`: GST Suvidha Provider API integrations.
  * `shop/`: Core domain logic for Shop management, customers, accounts, vouchers, ledger books, and items/inventory.

---

## 🔄 Integrations & Dependency Flow

The backend consumes API models and controller interfaces generated from the [lekhai-apispec](file:///Users/dream/Lekhai/lekhai-apispec) specification module.

1. When API specs are modified in `lekhai-apispec`, they are generated into Java interface files and published to the local maven cache (`~/.m2/repository`).
2. `LekhaiBackend` references these packages in [build.gradle](file:///Users/dream/Lekhai/LekhaiBackend/build.gradle):
   ```groovy
   implementation 'in.lekhai:lekhaiapispec:1.0.4' // or latest version from lekhai-apispec/VERSION
   ```
3. Controller classes in the backend implement interfaces generated under package `in.lekhai.contract.api` and accept models from `in.lekhai.contract.model`.

---

## 💻 Developer & Agent Commands

Run these commands from [LekhaiBackend/](file:///Users/dream/Lekhai/LekhaiBackend):

### Build and Run
* **Clean the project:**
  ```bash
  ./gradlew clean
  ```
* **Build the application (skipping tests):**
  ```bash
  ./gradlew build -x test
  ```
* **Run tests:**
  ```bash
  ./gradlew test
  ```
* **Run application locally:**
  ```bash
  ./gradlew bootRun
  ```

### Database & Migrations
* **Run Flyway migrations on the database:**
  ```bash
  ./gradlew flywayMigrate -Duser.timezone=UTC
  ```

### Containerization (Docker)
* **Build Docker image and load it into local Docker daemon:**
  ```bash
  ./gradlew jibDockerBuild
  ```
* **Build and push Docker image directly to registry:**
  ```bash
  ./gradlew jib
  ```

---

## 💡 Best Practices for Agents

1. **API Changes:** Do NOT write custom REST endpoint signatures or DTO classes directly in the backend. Update the spec in `lekhai-apispec`, compile/publish it to Maven local, and then implement the updated interface in the backend controller.
2. **Database Schema:** Always write migrations in `src/main/resources/db/migration/` using standard PostgreSQL SQL statements. Never modify existing migration scripts once they have been executed/applied.
3. **UTC Timezones:** Always run operations and database actions using the UTC timezone where possible (e.g. `flywayMigrate` requires `-Duser.timezone=UTC`).
4. **Clean Code:** Adhere to Spring Boot layered architecture (Controllers -> Services -> Repositories/JDBC helpers). Use validation annotations on request bodies.
