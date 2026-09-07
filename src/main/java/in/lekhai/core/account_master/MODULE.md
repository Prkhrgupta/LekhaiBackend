# core/account_master

The Chart of Accounts (खाता) — the master data of the accounting system. In Tally terms, this is the "Accounts Info" module. A shop records its books of accounts here before it can enter vouchers, print reports, or file GST.

## Business Goal

This module is the foundation every other accounting feature builds on. A shop captures:

- **Account groups** — the hierarchical, predefined structure of the ledger (like Tally's group tree: Assets, Income, Expenses, Liabilities, and their sub-groups). Each group gives its ledgers a nature (debit/credit) and a place in the books.
- **Ledgers** — the individual accounts a business actually posts to: cash, bank, customers (sundry debtors), suppliers (sundry creditors), sales, purchases, expenses. Every ledger carries its opening balance (so books carry forward from the previous year / previous software), credit limit, contact & tax details (GSTIN, PAN), and default area/broker/transporter so vouchers auto-fill.
- **Trade-party masters** — Area (sales territories), Broker (commission agents), Transport (goods carriers, with GST no). They attach to ledgers and feed both voucher entry and E-way bill generation.
- **States (system-level)** — GST codes for every Indian state, shared by all shops; used to determine CGST/SGST/IGST and resolve addresses.
- **Purchase & sale ledger settings** — for each purchase/sale ledger, which ledgers its tax components (CGST/SGST/IGST/cess), freight/packing, round-off, TDS (on purchases) and TCS (on sales) post to. This is what lets a purchase or sales voucher automatically split the bill into the correct duty & tax accounts.

Everything downstream depends on this module: `voucher/` posts to ledgers, `accountbooks/` reports from them, `csv/` bulk-loads these masters, and `gsp/` + `category/` validate GSTINs and move goods on the parties' behalf.

## Public API

Organised as the standard 4-layer pattern (controller → service → repository → domain) per master:

- `Ledger*` — the ledger of accounts (account group, GSTIN, address, opening balance, credit limit, defaults, PAN/TAN/email...).
- `AccountGroup*` — the group tree that structures all ledgers.
- `Area*` / `Broker*` / `Transport*` — trade-party masters for territories, commission agents, and goods carriers.
- `State*` — India GST states (system-level).
- `PurchaseLedgerSetting*` / `SaleLedgerSetting*` — duty & tax ledger configuration per purchase/sale ledger.
- `GstInDetails`, `Address` — tax-registration and address records owned by a ledger.
- `domain/LedgerSummaryProjection` — aggregate debit/credit/net balance of a ledger.
- `seeders/` — boot-time master seeding from `resources/seeds/*.csv`.
- `utils/LedgerUtils`, `utils/DateUtils` — ledger name/code and date helpers.

## Dependencies

- `common/` (`ShopAwareEntity`, `Result`), `shop/` (every shop-scoped master is RLS-isolated — never query without `ShopContext` active), `error/` (client/"not found" exceptions), `csv/` (bulk uploads persist into these masters).

## How to Extend

To add a new master to the books of accounts, follow this module's own pattern plus a Flyway migration:

1. `domain/<X>` — extend `ShopAwareEntity` unless system-level (like `State`).
2. `repository/<X>Repository` — Spring Data JDBC interface.
3. `service/<X>Service` — business rules and validation.
4. `controller/<X>Controller` — generated `*Api`, `@PreAuthorize` only.
5. Throw a domain exception from `error/` for not-found / validation cases.
6. If vouchers must post against the new master, keep `LedgerRepository`'s raw-SQL lookups compatible — they are consumed across `voucher/` and `accountbooks/`.

## Deep Dive

> Read `DETAILS.md` when: changing ledger balances/queries, account-group seeding, the ledger-settings behaviour used during voucher posting, or adding a master to the ledger-linked graph.