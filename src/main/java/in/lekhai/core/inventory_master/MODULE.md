# core/inventory_master

The **stock master** — the goods the firm trades. In Tally terms, this is the "Inventory Info / Stock Items" side of the books: the catalogue of what a shop buys and sells, before any purchase or sales voucher touches it.

## Business Goal

A trading firm's books aren't complete from ledgers alone — it must also know what it trades: the stock items, how they're grouped, and where they come from. This module captures that catalogue per shop:

- **Stock items** — each buyable/sellable unit of goods, with purchase price, sale price, rate per (piece/meter/…), and its **opening stock** (pieces, meters, rate, value — the inventory counterpart of a ledger's opening balance). A stock item references a **commodity**, an **item category**, and a **factory**.
- **Commodity** — the broad class of product being traded (e.g. yarn, fabric), used to group items.
- **Item category** — the finer classification an item falls under.
- **Item factory** — the manufacturing unit/plain of origin the item comes from (with a reference percentage).

Together these form the catalogue that purchase/sales flows will draw from for item-level entries, rates and stock valuation. It mirrors the same 4-layer pattern as `core/account_master/` for consistency.

## Public API

Organised as the standard 4-layer pattern (controller → service → repository → domain) per master:

- `Commodity*` — product-type master.
- `ItemCategory*` — item classification master.
- `ItemFactory*` — factory/unit-of-origin master.
- `StockItem*` — the stock-keeping item that aggregates commodity + category (+factory), with prices, rate-per, and opening stock.

## Dependencies

- `common/` (`ShopAwareEntity`, `Result`), `shop/` (RLS on every stock master), `error/` (validation exceptions when a stock item references a missing master).

## How to Extend

To add a new inventory master, follow this module's own pattern plus a Flyway migration:

1. `domain/<X>` — extend `ShopAwareEntity`.
2. `repository/<X>Repository` — Spring Data JDBC interface.
3. `service/<X>Service` — business rules; throw `error/` exceptions on invalid references.
4. `controller/<X>Controller` — generated `*Api`, `@PreAuthorize` only.
5. Add the migration (append-only, never edit an applied one — note `V19` deliberately dropped a commodity↔ledger linkage; do not reintroduce it without a fresh migration).

## Deep Dive

> Read `DETAILS.md` when: changing stock-item composition (which masters it references) or its relation to the ledger/ledger-settings.