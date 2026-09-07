# csv

The **bulk setup & migration desk** — how a firm gets its master data in without retyping it. In Tally terms, this is the "import" workflow: hand Lekhai a CSV of your existing book of accounts and it creates the ledgers, transporters, areas and brokers for you.

## Business Goal

Setting up a new shop (or migrating a firm from spreadsheets/another accounting package) means recording large lists of accounts and trade parties. Keying them in one at a time is slow and error-prone. This module imports these masters from CSV, batch by batch:

- **LEDGER** — the firm's ledger accounts (chart of accounts) — the biggest setup task.
- **TRANSPORT** — the goods carriers the firm dispatches with.
- **AREA** — the territories/areas the firm operates in.
- **BROKER** — the commission agents it deals through.

Each uploaded file is parsed into rows, validated per row (with de-duplication), and persisted into the appropriate master in `core/account_master/` — all within the shop's context so imported data stays tenant-isolated. Row-level problems are collected and reported through the standard error envelope rather than aborting the whole upload, so a partially-bad file still imports the good rows and tells the user which lines to fix.

## Public API

- `upload/controller/CsvUploadController` — file-upload endpoint; identifies the upload type and delegates.
- `upload/model/CsvUploadTypes` — the importable master kinds: `TRANSPORT`, `AREA`, `BROKER`, `LEDGER`.
- `upload/service/CsvUploadService<T,D>` — the contract every import implements: parse rows → map to the domain type → persist.
- `upload/factory/CsvUploadFactory` — routes each upload type to its import implementation (new implementations auto-register).
- `upload/service/implementations/Upload{Area,Broker,Ledger,Transport}Csv` — one import per master, with row-level validation and dedupe.
- `upload/dto/*CsvDto` — CSV row models.
- `upload/service/OpenCsvItemReader` — the batch item reader feeding the import pipeline.

## Dependencies

- `core/account_master/` (the masters that imports persist into), `common/` (`ShopAwareEntity` for shop-scoping), `error/` (row-level error collection), `shop/` (import runs inside shop context), OpenCSV, Spring Batch.

## How to Extend

To let the firm bulk-import another master:

1. Add the type constant in `CsvUploadTypes` + a row DTO (`@CsvBindByName`).
2. Implement `CsvUploadService<T,D>` under `upload/service/implementations/`.
3. Set the chunk/commit config and add the endpoint in `CsvUploadController`.
4. Keep per-row error collection so one bad row never kills the import.

## Deep Dive

> Read `DETAILS.md` when: changing the batch reader config, the upload pipeline, or the factories.