# Lekhai — New Screen Development Playbook

> **Purpose.** This is the master reference + prompt for building a new master/data-entry
> screen end-to-end across the three Lekhai repos. It was reverse-engineered from the
> **Commodity** screen (branch `inventory-master` in all three repos), which is the
> canonical worked example. When you (or an LLM agent) are handed an *old screen image*,
> follow this document to produce the spec, backend, and frontend changes that recreate it
> in the Lekhai stack — using the existing generic components, not hand-rolled code.
>
> Keep this file in sync whenever the patterns below change.

---

## 0. The three repos and how they fit together

```
/Users/sarveshnerurkar/Projects/Lekhai/
├── Lekhai Apispec/lekhai-apispec/   ← OpenAPI 3.0.3 spec. SINGLE SOURCE OF TRUTH for contracts.
│                                       Generates a Spring server-interface JAR + a Flutter client SDK.
├── Lekhai Backend/                  ← Java 21 / Spring Boot 3.4.1. Implements the generated interfaces.
│                                       Spring Data JDBC + Postgres Row-Level Security (multi-tenant).
└── Lekhai Frontend/                 ← Flutter (web-first). Dart pkg name `sitswift`.
                                        Consumes the generated Flutter client; Riverpod + go_router.
```

**Data/dependency flow (always in this order):**

```
1. Apispec    edit openapi/*.yaml  →  make all  →  bump VERSION  →  publish to Maven local + regen Flutter SDK
                    │                                   │                         │
                    ▼                                   ▼                         ▼
2. Backend    bump in.lekhai:lekhaiapispec    implement generated   *Api interface
                                               (Controller→Service→Repository, RLS migration)
                    │
                    ▼
3. Frontend   regenerated Flutter SDK is a local path dep → build datasource→repo→provider→screens→router
```

**Golden rule: contracts are never hand-written.** You do not create DTO classes in the
backend or model classes in the frontend. You edit the OpenAPI spec; both sides consume
generated code (`in.lekhai.contract.model.*` in Java, `package:openapi/openapi.dart` in Dart).

---

## 1. Reading a screen image → a data model

When given a legacy screen image, extract these before touching any repo:

1. **Entity name** (e.g. `Commodity`). Pick: snake table name (`commodity_master`), package
   group (`inventory_master`, `account_master`, …), API tag (`Commodity`), base path (`/commodity`).
2. **Form fields** — for each: label, type (text / decimal / integer / dropdown / toggle /
   ledger-account picker / date), required?, and whether it's *flat* or part of a *nested group*
   (e.g. Commodity's "Sale Ledger → In-State → CGST%").
3. **Conditional sections** — fields that enable/disable based on a toggle (Commodity disables
   the whole "Ledger Accounts" section when `sale_purchase_setting` = No).
4. **List/grid columns** — which subset of fields appears in the summary table, their widths
   and types (`number`/`text`), and which fields are searchable.
5. **Relationships** — FK references to other masters (Commodity references `ledger(id)` for
   every account picker).

This maps onto the **standard screen shape**: a *paginated list (grid) screen* + a
*create/edit form screen*, with CRUD + a `/summary` endpoint.

---

## 2. The standard endpoint set (per master)

Every master exposes the same operations. The **current** template is the
single-field masters (`Area`, `Broker`, `Transport`) plus `Commodity`'s soft-delete —
the newest reference is the Item Category / Item Factory pair (`inventory_master`):

| Method | Path | operationId | Returns |
|---|---|---|---|
| POST | `/<entity>` | `create<Entity>` | `<Entity>Response` |
| GET | `/<entity>` | `get<Entity>DropdownOptions` | `array<DropdownItem>` (un-paged, for dropdowns) |
| GET | `/<entity>/{id}` | `get<Entity>` | `<Entity>Response` |
| PUT | `/<entity>/{id}` | `update<Entity>` | `<Entity>Response` |
| DELETE | `/<entity>/{id}` | `delete<Entity>` | `204 No Content` (soft delete) |
| GET | `/<entity>/summary` | `get<Entity>Summaries` | `<Entity>SummaryPageResponse` (paginated grid feed) |

Notes:
- DELETE is a **soft delete** — the row carries an `is_deleted` flag; the endpoint
  returns `204` and the service flips the flag rather than removing the row.
  (Older masters like `Area` predate the DELETE op; add it for new screens.)
- The plain GET list returns **`DropdownItem`** (`{ id, label }`), not the full Response.
- The `/summary` endpoint is the grid's data source and is **paginated + searchable**.
  Its response is **`<Entity>SummaryPageResponse { data: [<Entity>Response], pagination }`**
  — it reuses `<Entity>Response` in `data` and carries **no server-side `columns`**.
  (See §3.3 — the older `Commodity` columns/`SummaryItem` shape is legacy.)

---

## 3. REPO 1 — Apispec (`Lekhai Apispec/lekhai-apispec`)

### 3.1 Layout & ref-wiring
The spec is split into many fragments assembled by `$ref`. Read `CLAUDE.md` there for the
full mechanics. Key files:
- `openapi/root.yaml` — top-level doc; version is the literal `{{VERSION}}` placeholder.
- `openapi/paths.yaml` — **flat registry**: every public route maps to a JSON-pointer ref into
  a module's `paths.yaml`. Slashes escaped as `~1`. **You must add an entry here for each route.**
- `openapi/<domain>/<entity>/paths.yaml` + `components.yaml` — the module pair (operations + schemas).
- `openapi/common/components.yaml` — shared `responses` (`UnauthorizedError`, `ForbiddenError`,
  `InternalServerError`, `NotFoundError`), `PaginationMeta`, `DropdownItem`, `Error`, `Format`.

Ref conventions: within a fragment, schemas use `'#/SchemaName'` (no `components/schemas`
prefix). Cross-module: relative path like `'../../common/components.yaml#/responses/UnauthorizedError'`.

### 3.2 Steps to add a screen's contract
1. Create `openapi/<domain>/<entity>/paths.yaml` and `components.yaml`.
   Copy `openapi/inventorymaster/commodity/` as the starting template.
2. Define schemas in `components.yaml`:
   - `<Entity>Request` (required fields under `required:`), `<Entity>Response` (adds `id`).
     Use nested sub-schemas for grouped sections (e.g. `SaleLedgerRequest` → `SaleInStateRequest`).
   - `<Entity>SummaryPageResponse` = `{ data: [<Entity>Response], pagination: PaginationMeta }`.
     **Reuse `<Entity>Response` in `data`** — do not define a separate `SummaryItem`, and do
     **not** put `columns` on the server (the frontend owns columns; see §5.6).
   - `<Entity>SearchableField` = `enum` of fields the grid can search (e.g. `[NAME, HSN_SAC_CODE]`).

   > Legacy: `Commodity` still uses `CommoditySummaryResponse { columns, data: [SummaryItem] }`
   > with `SummaryColumn`/`SummaryItem` schemas. New screens must not copy that — follow the
   > `SummaryPageResponse` shape above (Area / Broker / Transport / Item Category / Item Factory).
3. Define the 5 operations in `paths.yaml`, all tagged `[<Entity>]` (the tag drives Spring API
   grouping — keep it consistent). Reference common error responses for 401/403/500.
   For the summary operation add `x-spring-paginated: true` plus `page`/`size`/`sort`/
   `searchableField`/`searchText` query params (copy Commodity's verbatim).
4. **Register every route in `openapi/paths.yaml`** (`/commodity`, `/commodity/{id}`,
   `/commodity/summary`).
5. Field naming: spec uses **snake_case** property names (`hsn_sac_code`, `gst_rate_sale`).
   The generators camel-case them for Java/Dart (`hsnSacCode`, `gstRateSale`).
6. Validate & generate:
   ```bash
   make lint && make bundle      # validate
   make all                      # lint→bundle→generate→flutter-build (regen both SDKs)
   ```
7. **Bump `VERSION`** (e.g. `1.0.6` → `1.0.7`) — this is the published artifact version.
8. Publish the Spring artifact to Maven local so the backend can pick it up:
   ```bash
   ./gradlew publishToMavenLocal    # publishes in.lekhai:lekhaiapispec:<VERSION>
   ```
   The Flutter SDK is consumed directly from `generated/flutter` via a path dependency, so
   `make all` already refreshed it.

### 3.3 The summary contract — use the SummaryPageResponse shape
The **current default** is `<Entity>SummaryPageResponse { data: [<Entity>Response], pagination }`
— no server-side `columns`, and `data` reuses the full `<Entity>Response` (no `SummaryItem`).
Used by `Ledger`, `Area`, `Broker`, `Transport`, `Item Category`, `Item Factory`.

- **Legacy (do not copy):** `Commodity` returns `CommoditySummaryResponse { columns, data: [SummaryItem], pagination }`
  with server-delivered columns and a separate flat `SummaryItem`. This predates the cleanup
  (apispec commit "removed unnecessary SummaryItems") and should be migrated when touched.

The frontend defines grid columns locally regardless (see §5.6), so the server never needs to
send them.

---

## 4. REPO 2 — Backend (`Lekhai Backend`)

Package root for a master: `in.lekhai.core.<domain>.<layer>`, e.g.
`in.lekhai.core.inventory_master.{domain,repository,service,controller}`.

### 4.1 Bump the spec dependency
In `build.gradle`: `implementation 'in.lekhai:lekhaiapispec:<NEW_VERSION>'`.

### 4.2 Domain entity (`domain/<Entity>.java`)
- Extend `ShopAwareEntity` (gives `shopCode`, `createdAt`, `updatedAt` + auto shop stamping).
- `@Table("<entity>_master")`, `@Id` on the PK (Commodity uses `itemId` → `item_id`).
- Plain fields with getters/setters. Money/percentages → `BigDecimal`. FK ids → `Long`.
- Carry a `Boolean isDeleted = false` for soft delete.
- **Spring Data JDBC, not JPA** — no lazy loading, no relations; load aggregates explicitly.

### 4.3 Repository (`repository/<Entity>Repository.java`)
- `@Repository interface <Entity>Repository extends CrudRepository<<Entity>, Long>`.
- For the paginated/searchable summary add (copy from `LedgerRepository`):
  ```java
  Page<Entity> findAll(Pageable pageable);
  @Query("SELECT * FROM <table> WHERE name ILIKE '%' || :query || '%'")
  List<Entity> findByNameContainingIgnoreCase(@Param("query") String q, Pageable p);
  @Query("SELECT COUNT(*) FROM <table> WHERE name ILIKE '%' || :query || '%'")
  long countByNameContainingIgnoreCase(@Param("query") String q);
  ```

### 4.4 Service (`service/<Entity>Service.java`)
- `@Service`; **every DB method annotated `@ShopContextTransactional`** (NOT plain
  `@Transactional`) — RLS shop code is set per-transaction, so reads/writes must run in one.
- Implement: `create`, `update` (findById→map→save, throw `<Entity>NotFoundException` if missing),
  `delete<Entity>` (soft delete — set `is_deleted=TRUE`, save), `getById`, `list` (→ `List<DropdownItem>`,
  filter out soft-deleted), and `list<Entity>Summaries(...)`.
- Mapping is hand-written `mapToEntity(request, entity)` / `mapToResponse(entity)` helpers,
  including null-safe `BigDecimal`↔`Double` converters. (See `CommodityService` for the nested
  sale/purchase ledger mapping; `ItemFactoryService` for a minimal `percentage` converter.)
- **Summary (paginated/searchable)** — follow `AreaService`/`ItemCategoryService.list<Entity>Summaries`,
  mapping the page content back into `<Entity>Response` (not a separate SummaryItem):
  ```java
  Page<Entity> page = (searchText present && field == NAME)
      ? new PageImpl<>(repo.findActiveByNameContainingIgnoreCase(q, pageable),
                       pageable, repo.countActiveByNameContainingIgnoreCase(q))
      : new PageImpl<>(repo.findAllActive(pageable), pageable, repo.countAllActive());
  return new <Entity>SummaryPageResponse()
      .data(page.getContent().stream().map(this::mapToResponse).toList())
      .pagination(new PaginationMeta().page(page.getNumber()).size(page.getSize())
          .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()));
  ```

### 4.5 Controller (`controller/<Entity>Controller.java`)
- `@RestController` `implements <Entity>Api` (the generated interface). **No `@RequestMapping`**
  — paths come from the spec.
- Class-level `@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)` (or appropriate constant:
  `IS_SUPER_ADMIN`, `NOT_SUPER_ADMIN`).
- Constructor-inject the service; each method logs with `ShopContext.getShopCode()`, delegates,
  returns `ResponseEntity.ok(...)`.
- The paginated summary method signature matches the generated interface — for the Ledger style
  it is `getXSummaries(@Valid XSearchableField field, @Valid String query, Pageable pageable)`.
- Errors: throw `LekhaiException` / `LekhaiClientException` subclasses (under
  `error/controller/**/exception/`); `GlobalExceptionHandler` maps them. Don't build error
  `ResponseEntity` bodies by hand. (Commodity currently throws `RuntimeException` for
  not-found — for new screens use a proper domain `NotFoundException` instead.)

### 4.6 Migration (`src/main/resources/db/migration/V<n>__create_<entity>_master_table.sql`)
- New `V<n>__...sql` file (never edit an applied migration). Plain PostgreSQL.
- `BIGSERIAL PRIMARY KEY`; columns match entity; money `NUMERIC(10,2)`, pct `NUMERIC(5,2)`.
- FK references to other masters (`... BIGINT REFERENCES ledger(id)`).
- Audit + tenancy columns (copy from `V10`):
  ```sql
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_deleted BOOLEAN DEFAULT FALSE,
  shop_code INTEGER NOT NULL DEFAULT current_setting('app.shop_code', false)::INTEGER
  ```
- Indexes on searchable columns.
- **Enable RLS** (mandatory for tenant tables):
  `CALL create_shop_isolation_policy('<table>', 'shop_isolation_<table>');`
- Apply: `./gradlew flywayMigrate -Duser.timezone=UTC`.

### 4.7 Build & verify
```bash
./gradlew build -x test      # compile against the new generated interface
./gradlew bootRun            # run on :8080
```

---

## 5. REPO 3 — Frontend (`Lekhai Frontend`, pkg `sitswift`)

Feature lives under `lib/features/<domain>/`. **Read `lib/widgets/README.md` and the
root `AGENTS.md`** — they document the form system and the `PaginatedPlutoGrid` in depth.
Assemble screens from the shared toolkit; never hand-roll `TextFormField`s or grids.

### 5.1 The generated client is a local path dep
`pubspec.yaml` points `openapi` at
`…/Lekhai Apispec/lekhai-apispec/generated/flutter`. After `make all` in apispec,
run `flutter pub get`. All models/enums/`*Api` come from `package:openapi/openapi.dart`.

### 5.2 Feature file map (mirror Commodity)
```
lib/features/<domain>/
├── data/datasources/<entity>_remote_datasource.dart   thin wrapper over the generated <Entity>Api
├── data/repositories/<entity>_repository_impl.dart      orchestration/passthrough
└── presentation/
    ├── providers/<entity>_provider.dart                 Riverpod StateNotifier (+ BaseProvider mixin)
    ├── utils/<entity>_request_helper.dart               flat formData ⇄ nested Request/Response mappers
    ├── utils/<entity>_summary_columns.dart              PlutoColumn list + SummaryItem→PlutoRow extension
    └── screens/<entity>/
        ├── create_<entity>_screen.dart                  the DynamicFormBuilder form (create/modify/view)
        └── edit_<entity>_screen.dart                    the PaginatedPlutoGrid list screen
```

### 5.3 Datasource + Repository
- Datasource: one method per API call, `await _api.method(...)` then `return response.data!`.
  Include the summary method with `{int? page, int? size, <Entity>SearchableField? searchableField, String? searchText}`.
- Repository: passthrough to the datasource (kept for the clean-architecture seam).

### 5.4 DI wiring (3 edits — easy to forget)
- `lib/core/api/openapi_providers.dart`: add `final <entity>ApiProvider = Provider<<Entity>Api>((ref) => ref.watch(openapiProvider).get<Entity>Api());`
- `lib/core/api/api_repo_providers.dart`: add the `<entity>RemoteDatasourceProvider` and
  `<entity>RepositoryProvider`.
- (Provider file below.)

### 5.5 State provider (`providers/<entity>_provider.dart`)
- `StateNotifierProvider` over an `<Entity>State { status, item, items, error }` with
  `copyWith`. Notifier extends `StateNotifier` `with BaseProvider` (gives `handleError(e)` for
  `DioException` parsing). Methods: `fetchById`, `create`, `update`, `fetchAll`, `clear`.
  Export a `<entity>NotifierProvider` alias.

### 5.6 Grid list screen (`edit_<entity>_screen.dart`)
- `ConsumerStatefulWidget` holding a `GlobalKey<PaginatedPlutoGridState<<Entity>SearchableField>>`.
- Body = `PaginatedPlutoGrid<<Entity>SearchableField>` (generic widget at
  `lib/widgets/grid/paginated_pluto_grid.dart`):
  ```dart
  PaginatedPlutoGrid<XSearchableField>(
    key: _gridKey,
    columns: xSummaryColumns(),
    fetchPage: (page, pageSize, field, text) async {
      final res = await ref.read(xRepositoryProvider).getXSummaries(
        page: page - 1, size: pageSize, searchableField: field, searchText: text);
      return PlutoLazyPaginationResponse(
        totalPage: res.pagination?.totalPages ?? 1,
        rows: res.data?.map((e) => e.toPlutoRow()).toList() ?? []);
    },
    onRowDoubleTap: _onRowTapped,                       // load by id → pushNamed the form
    searchableFields: XSearchableField.values.toList(),
    searchFieldLabel: (f) => f.name,
    infoBanner: /* optional hint banner */,
  );
  ```
  Note: grid pages are **1-based**; backend is **0-based** → pass `page - 1`.
- `_onRowTapped` reads `row.cells['id']`, shows a loading dialog, fetches the full entity via the
  provider, then `context.pushNamed('create_<entity>', extra: {'initialData': entity, 'mode': FormMode.modify})`,
  and `_gridKey.currentState?.refresh()` if the form returns `true`.
- **Columns file** (`utils/<entity>_summary_columns.dart`): a top-level
  `List<PlutoColumn> xSummaryColumns()` plus an `extension XSummaryItemX on XSummaryItem`
  with `toPlutoRow()`. **Cell keys must exactly match `PlutoColumn.field`.** Set
  `enableEditingMode: false` on read-only columns. Use `renderer: wrapCellText` for wrapping.

### 5.7 Form screen (`create_<entity>_screen.dart`)
- `ConsumerStatefulWidget` with `GlobalKey<DynamicFormBuilderState>`; params
  `{<Entity>Response? initialData, FormMode mode}`.
- Body = `DynamicFormBuilder` (`lib/widgets/form/form_builder.dart`) composed of
  `LekhaiFormSection` → `LekhaiFormRow` → fields. **This is the core reusable form engine** —
  it owns controllers, focus, validation, Enter-to-advance, and create/modify/view modes.
- **Field name = the map key.** `initialData[name]` seeds the value; `onSubmit` receives a
  `Map<String,dynamic>` keyed by `name`. `order` drives both visual order and keyboard
  traversal (global). `flex` is a 1–12 column width.
- Field catalog (`lib/widgets/form/fields/`, all extend `FormFieldBase`):
  | Need | Field |
  |---|---|
  | text / HSN / decimal-as-text | `CustomTextFormField` (+ `keyboardType`, `inputFormatters`) |
  | INR currency | `CustomAmountFormField` |
  | Yes/No or cycling toggle | `CycleFormField` (`staticDataKey: StaticDataKey.yesNo`) |
  | dropdown over local data | `StaticSearchableDropDownField` (`staticDataKey: ...`) |
  | dropdown backed by an API (e.g. ledger picker) | `CustomAsyncSearchableDropDownField` (`fetcher:`) |
  | file upload | `CustomFileUploadField` |
  | multi-line address | `CompactMultiLineField(...).toRows()` |
  | arbitrary widget / column header / spacer | `CustomSlot` |
- **Static dropdown data** lives in `lib/common/presentation/providers/dropdown_providers.dart`:
  add a value to the `StaticDataKey` enum and an entry in `staticDropdownData` (Commodity added
  `unitOfMeasurement`).
- **Conditional enable/disable:** keep a `bool _ledgerFieldsEnabled`, toggle it in the
  controlling field's `onChanged`, `setState`, and pass `enabled: _ledgerFieldsEnabled` to the
  dependent fields (Commodity's Ledger Accounts section).
- **Two-column layouts** (Sale | Purchase): one `LekhaiFormRow` with paired fields; use
  `CustomSlot` helpers for column headers/spacers.
- `onSubmit`: build the request via the request-helper, call repo `create`/`update`, show a
  toast via `ToastHelper`, then `Navigator.of(context).pop(true)`.

### 5.8 Request/Response mapping (`utils/<entity>_request_helper.dart`)
The form is **flat**; the openapi model is **nested + built_value**. The helper bridges them:
- `fromFormData(Map) → <Entity>Request` using `<Entity>RequestBuilder()` and
  `..nested.replace(SubBuilder(...))`. Use the lenient parsers in
  `lib/core/utils/parse_helpers.dart` (`parseInt`, `parseDouble`) — they return `null` for
  empty input so unset fields stay unset.
- `toFormData(<Entity>Response) → Map` for editing; null-coalesce every field to `''`.
- Document the flat naming convention in a doc comment (e.g. `sale_*`/`purchase_*` prefixes,
  `*_pct`, `*_account`, `*_instate`/`*_outstate`) as Commodity does.

### 5.9 Routing (`lib/core/router.dart`)
Add two `GoRoute`s inside the single `ShellRoute` (copy Commodity's):
```dart
GoRoute(
  path: '/<domain>/<entity>/create', name: 'create_<entity>',
  pageBuilder: (c, s) {
    final args = s.extra as Map<String, dynamic>?;
    final init = args?['initialData'];
    return NoTransitionPage(child: XFormScreen(
      initialData: init is XResponse ? init : null,
      mode: args?['mode'] ?? FormMode.create));
  }),
GoRoute(
  path: '/<domain>/<entity>/edit', name: 'edit_<entity>',
  pageBuilder: (c, s) => NoTransitionPage(child: const EditXScreen())),
```
The list screen is reached either via a static route or the **dynamic menu** (`/menu/:menuId`,
backend-driven — the menu item's `route` points here; see `features/menu/` and
`docs/menu-configuration.md` in the backend for wiring a sidebar entry).

### 5.10 Verify
```bash
flutter pub get        # MUST succeed — picks up regenerated openapi models
flutter analyze        # lint
flutter run -d chrome  # web is the primary target
```

---

## 6. Naming conventions cheat-sheet (entity = "Commodity")

| Layer | Convention | Example |
|---|---|---|
| Spec property | snake_case | `hsn_sac_code` |
| Spec schema | `<Entity>Request/Response/SummaryItem/SearchableField` | `CommoditySummaryItem` |
| Spec tag / operationId | `[Commodity]` / `createCommodity` | |
| Route | `/<entity>`, `/<entity>/{id}`, `/<entity>/summary` | `/commodity/summary` |
| Backend table | `<entity>_master` | `commodity_master` |
| Backend package | `in.lekhai.core.<domain>` | `…core.inventory_master` |
| Backend pk | per entity | `item_id` / `itemId` |
| Generated Java model | `in.lekhai.contract.model.*` (camelCase fields) | `CommodityRequest.getHsnSacCode()` |
| Flutter feature dir | `lib/features/<domain>/` | `lib/features/inventory_master/` |
| Flutter route names | `create_<entity>`, `edit_<entity>` | `create_commodity` |
| Flutter form field key | snake_case (matches nothing generated — it's your own map key) | `sale_cgst_pct` |
| Grid cell key ⇄ column field | must be identical | `'hsn_sac_code'` |

---

## 7. End-to-end checklist (tick every box)

**Apispec**
- [ ] `paths.yaml` + `components.yaml` for the module (Request/Response/SummaryResponse/SummaryColumn/SummaryItem/SearchableField).
- [ ] Routes registered in `openapi/paths.yaml`.
- [ ] Operations tagged `[<Entity>]`; summary has `x-spring-paginated` + query params; errors ref common responses.
- [ ] `make lint && make all` clean; `VERSION` bumped; `publishToMavenLocal`.

**Backend**
- [ ] `build.gradle` apispec version bumped.
- [ ] Entity extends `ShopAwareEntity`; Repository; Service (`@ShopContextTransactional`, mappers, paginated summary); Controller `implements <Entity>Api` + `@PreAuthorize`.
- [ ] `V<n>__create_<entity>_master_table.sql` with audit cols + `shop_code` default + `create_shop_isolation_policy` + indexes.
- [ ] `flywayMigrate` run; `build -x test` green.

**Frontend**
- [ ] `flutter pub get` picks up regenerated models.
- [ ] datasource + repository + provider; request_helper; summary_columns (+ `toPlutoRow`).
- [ ] form screen (DynamicFormBuilder) + grid screen (PaginatedPlutoGrid).
- [ ] DI: `openapi_providers.dart`, `api_repo_providers.dart`.
- [ ] static dropdown data added if needed; routes added in `router.dart`; menu entry if applicable.
- [ ] `flutter analyze` clean; manual run verifies create/list/edit.

---

## 8. Reusable generic components — quick index

**Frontend (`lib/widgets/`):**
- `form/form_builder.dart` — `DynamicFormBuilder` (the form engine). Docs: `lib/widgets/README.md`.
- `form/layout/{form_section,form_row}.dart` — `LekhaiFormSection`, `LekhaiFormRow`.
- `form/fields/*` — the field catalog (§5.7).
- `form/validators/common_validator.dart` — validators + input formatters.
- `slots/custom_slots.dart` — `CustomSlot` for arbitrary widgets in the form.
- `grid/paginated_pluto_grid.dart` — `PaginatedPlutoGrid<T>` (lazy paginated grid + search). Docs: root `AGENTS.md`.
- `grid/pluto_grid_search_bar.dart`, `grid/pluto_grid_widget.dart` — grid internals (themed).
- `searchable_dropdown_field.dart` — dropdown primitives.
- `core/utils/parse_helpers.dart` (parsing), `core/utils/toast_helper.dart` (toasts),
  `core/app_theme.dart` (`context.colors`, `context.fontScale`), `core/providers/base_provider.dart` (`handleError`).
- `common/presentation/providers/dropdown_providers.dart` — `StaticDataKey` + static option data.

**Backend (common infra):**
- `common/domain/ShopAwareEntity` — base entity (shop stamping + audit).
- `shop.context.transaction.manager.annotation.@ShopContextTransactional` — tenant-aware tx.
- `authentication.utils.SecurityExpressions` — `@PreAuthorize` constants.
- `error/.../GlobalExceptionHandler` + `LekhaiException`/`LekhaiClientException` hierarchy.
- `db/migration/V6__create_rls_procedures.sql` → `create_shop_isolation_policy(table, policy)`.

**Apispec (shared):**
- `openapi/common/components.yaml` — `PaginationMeta`, `DropdownItem`, `Error`, `Format`, common `responses`.

---

## 9. Ready-to-use prompt (paste this, attach the screen image)

> **Task:** Build the **`<ENTITY>`** master screen end-to-end across the three Lekhai repos,
> following `SCREEN_DEVELOPMENT_PLAYBOOK.md`. The attached image is the legacy screen to recreate.
>
> 1. **Analyse the image** per §1: list the entity name, every form field (label, type,
>    required, flat vs nested group, conditional rules), the grid columns + searchable fields,
>    and FK relationships. Restate this as a data model and wait for / assume confirmation.
> 2. **Apispec** (§3): add `openapi/<domain>/<entity>/{paths,components}.yaml`, register routes
>    in `paths.yaml`, tag `[<Entity>]`, paginated+searchable `/summary`. `make all`, bump `VERSION`,
>    `publishToMavenLocal`.
> 3. **Backend** (§4): bump the apispec dep; create Entity (`ShopAwareEntity`) + Repository +
>    Service (`@ShopContextTransactional`, mappers, paginated summary à la `LedgerService`) +
>    Controller (`implements <Entity>Api`, `@PreAuthorize`); add the `V<n>` migration with
>    audit/`shop_code` cols, RLS policy, indexes; `flywayMigrate`; `build -x test`.
> 4. **Frontend** (§5): `flutter pub get`; datasource→repository→provider→request_helper→
>    summary_columns→form screen (`DynamicFormBuilder`)→grid screen (`PaginatedPlutoGrid`);
>    wire DI (`openapi_providers`, `api_repo_providers`), static dropdowns, and `router.dart`
>    routes (`create_<entity>`/`edit_<entity>`); `flutter analyze`.
> 5. Use **only** the generic components in §8 — no hand-written DTOs, controllers, inputs, or grids.
> 6. Work through the §7 checklist and report what was done per repo.
>
> Reference implementation to copy patterns from: **Commodity** (`inventory_master`) on the
> `inventory-master` branch in all three repos; **Ledger** for the paginated-summary pattern.
