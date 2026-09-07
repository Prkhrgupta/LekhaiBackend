# core/account_master

Account & master-data domain: account groups, ledger, area, broker, transport, state, GST details, and purchase/sale ledger settings. Largest module.

## Public API (by sub-layer)

**Controllers** (`controller/`): `AccountGroupController`, `AreaController`, `BrokerController`, `LedgerController`, `PurchaseLedgerSettingController`, `SaleLedgerSettingController`, `StateController`, `TransportController`

**Services** (`service/`): `AccountGroupService`, `AreaService`, `BrokerService`, `LedgerService`, `PurchaseLedgerSettingService`, `SaleLedgerSettingService`, `StateService`, `TransportService`

**Domains** (`domain/`): `AccountGroup`, `Area`, `Address`, `Broker`, `GstInDetails`, `Ledger`, `LedgerSummaryProjection`, `PurchaseLedgerSetting`, `SaleLedgerSetting`, `State`, `Transport`

**Repositories** (`repository/`): matching 10 repos (`AccountGroupRepository`, `AddressRepository`, ..., `TransportRepository`)

**Seeders** (`seeders/`): `AccountGroupSeeder`, `GroupSeeder`, `GroupDTO`, `StatesSeeder`

**Utils** (`utils/`): `DateUtils`, `LedgerUtils`

## Dependencies

- `common/` (`ShopAwareEntity`, `Result`), `shop/`, `error/`, `csv/` (uploads persist here)

## How to Extend

Add a new account master:
1. `domain/<X>.java` (extend `ShopAwareEntity`), `repository/<X>Repository.java`, `service/<X>Service.java`, `controller/<X>Controller.java`.
2. Flyway migration for the table.
3. Add exception in `error/` if new not-found/validation cases appear.

## Deep Dive

> Read `DETAILS.md` only when: touching ledger queries/balances, seeding logic, or the ledger-settings interplay used by vouchers.