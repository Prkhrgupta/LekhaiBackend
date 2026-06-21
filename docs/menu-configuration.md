# Menu Configuration

How the Lekhai backend builds the sidebar menu, and how to add / debug menu items.

Source files (in `Lekhai Backend`):
- `core/controller/menu/MenuController.java` — `GET /menu`, `GET /topbar`
- `core/service/menu/MenuService.java` — resolves the user's enabled bits
- `core/service/menu/MenuBuilder.java` — builds the tree from features
- `core/controller/feature/FeatureController.java` — `POST /api/feature/create`, `GET /api/feature/all`
- `core/controller/category/CategoryController.java` — `POST /api/category/enable/features`, etc.
- `core/service/feature/FeatureService.java`, `FeatureMapService.java`
- `core/util/PermissionBitCalculator.java`
- `core/domain/feature/Features.java`, `core/repository/feature/FeaturesRepo.java`

All `/api/feature/*` and `/api/category/*` endpoints require **SUPER_ADMIN**.

## Core model

The whole menu is driven by the `features` table. Each row is either:
- a **group / container**: `is_screen = false` → no `bit_position`, no `route`. Rendered as an expandable branch when it has visible children.
- a **screen**: `is_screen = true` → gets an auto-assigned `bit_position` and a generated `route`. Rendered as a clickable leaf.

`features` columns: `id`, `feature_key`, `parent_id`, `title`, `icon`, `route`, `bit_position`, `display_order`, `is_active`, `is_deleted`.

Hierarchy is via `parent_id`. Route for a screen is generated from the parent chain, e.g. `Inventory Master > Commodity > Create` → `/inventory_master/commodity/create` (`FeatureService.generateRouteForScreenFeature`).

Bit position assignment: `findNextAvailableBitPosition()` = `MAX(bit_position) + 1` across all features (starts at 0). Bits are global, not per-section.

## How a menu item becomes visible

`MenuService.generateMenu()`:
1. Looks up the user, their `category_id`, role (from JWT), and shop access.
2. Computes final permission bits = **category AND role AND user-shop-access** (`PermissionBitCalculator.calculateFinalPermissions`).
   - Defaults when a list is shorter than the bit's word index:
     - category → all bits ON (`Long.MAX_VALUE`)
     - role → all ON **only for SHOP_OWNER**, else 0
     - user-shop-access → all ON
3. Extracts enabled bit positions, fetches the screen features for those bits (`findByBitPositionIn`).
4. `MenuBuilder` walks up `parent_id` to pull in parent groups, then renders the tree. A feature with children → branch (its own `route` is nulled); no children → leaf with its `route`.

So a screen shows up **iff its bit is enabled in ALL of category ∩ role ∩ user** (with the defaults above).

## API surface (and its limits)

Available:
- `GET /api/feature/all` — lists **only screen features** (those with a bit). Group rows are NOT returned. (`FeatureMapService.getRootFeatures` filters `bit_position != null`.)
- `POST /api/feature/create` — body: `{ featureKey, parentId, title, icon, isScreen }`. Returns `{ id, featureKey, bitPosition, title, icon, route }`.
- `GET /api/category/list-all`
- `POST /api/category/enable/features` — body: `{ categoryIdList: [..], bitsPositionsToBeEnabled: [..] }`. Only turns bits **ON**.

NOT available (must be done directly in the DB):
- No edit / delete / move / reorder feature endpoint.
- No way to turn a permission bit **off** via API.

## Gotchas (read before debugging)

- **`MenuBuilder` and `getFeaturesByBitPositions` ignore `is_active` and `is_deleted`.** Soft-deleting a feature does NOT hide it from the menu. The only visibility lever is the permission bit. To truly remove a menu item you must hard-delete the row (or never enable its bit).
- `feature/all` hides group rows, so to inspect the real tree use SQL:
  ```sql
  SELECT id, feature_key, title, parent_id, bit_position FROM features ORDER BY id;
  ```
- `enable/features` is additive only. To disable a bit you must edit the relevant permissions in the DB (`categories`, `role_permissions`, `user_shop_access` permission bitmask columns).
- Role permissions have no API in this repo; they're seeded / managed outside it. If a screen shows for SHOP_OWNER but not other roles, suspect the role bitmask ANDing it off.
- Bit positions are never reused automatically; deleting a screen leaves a gap (harmless).

## Recipe: add a new section with screens

Example: `Inventory Master > Commodity > Create / Edit` (mirrors `Account Master > Ledger > Create / Edit`).

1. Create the top group (container):
   ```
   POST /api/feature/create
   { "featureKey": "inventory_master", "parentId": null, "title": "Inventory Master", "icon": "<icon>", "isScreen": false }
   ```
2. Create the sub-group:
   ```
   POST /api/feature/create
   { "featureKey": "commodity", "parentId": <inventory_master id>, "title": "Commodity", "icon": "<icon>", "isScreen": false }
   ```
3. Create the screens (each gets a bit + route):
   ```
   POST /api/feature/create
   { "featureKey": "create", "parentId": <commodity group id>, "title": "Create", "icon": "<icon>", "isScreen": true }
   POST /api/feature/create
   { "featureKey": "edit", "parentId": <commodity group id>, "title": "Edit", "icon": "<icon>", "isScreen": true }
   ```
4. Enable the new bits for the right categories:
   ```
   GET  /api/category/list-all
   POST /api/category/enable/features
   { "categoryIdList": [<cat ids>], "bitsPositionsToBeEnabled": [<create bit>, <edit bit>] }
   ```

Use separate bits per action (create/edit/...) for independent permission control — this is the existing convention.

## Recipe: fix "X inside X" (duplicate nested level)

Symptom: a group and a screen share the same name, e.g. `Commodity (group) > Commodity (screen)`.

1. Inspect the tree (groups hidden from `feature/all`):
   ```sql
   SELECT id, feature_key, title, parent_id, bit_position FROM features ORDER BY id;
   ```
2. Hard-delete the redundant inner screen (no API; soft-delete won't hide it):
   ```sql
   DELETE FROM features WHERE id = <inner screen id>;
   ```
3. Create the intended screens under the group (step 3 above), then enable their bits (step 4).

## Debugging checklist: "menu item not showing"

1. Does the screen feature row exist with a `bit_position`? (`SELECT ... FROM features`)
2. Is that bit enabled for the user's **category**? (`categories.permissions`)
3. Is it enabled for the user's **role**? (`role_permissions`, ANDed; non-owners default to 0 for high words)
4. Is it enabled in **user_shop_access**?
   Final = category AND role AND user (see `PermissionBitCalculator`).
5. Is the parent chain intact (`parent_id`)? A broken parent link drops the item.
6. Remember: `is_active` / `is_deleted` do NOT affect visibility — bit must be off to hide.
