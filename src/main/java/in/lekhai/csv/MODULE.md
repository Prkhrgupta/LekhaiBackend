# csv

Spring Batch CSV uploads for master data: transport, area, broker, ledger.

## Public API

- `upload/controller/CsvUploadController` — file upload endpoints.
- `upload/service/CsvUploadService<T,D>` — base service interface (parse → validate → persist).
- `upload/factory/CsvUploadFactory` — routes upload type → service implementation.
- `upload/model/CsvUploadTypes` — enum: `TRANSPORT`, `AREA`, `BROKER`, `LEDGER`.
- `upload/service/implementations/UploadAreaCsv`, `UploadBrokerCsv`, `UploadLedgerCsv`, `UploadTransportCsv`.
- `upload/service/OpenCsvItemReader` — OpenCSV-based batch item reader.
- `upload/dto/*` — `AreaCsvDto`, `BrokerDto`, `LedgerCsvDto`, `TransportDto` (CSV row models).

## Dependencies

- `common/`, `error/`, `shop/`, `core/account_master/` (master repos), OpenCSV, Spring Batch

## How to Extend

Add a CSV upload type:
1. Add `CsvUploadTypes` constant + row DTO.
2. Implement `CsvUploadService<T,D>` (submit in `implementations/`).
3. Factory auto-registers via injected beans; add endpoint in `CsvUploadController`.

## Deep Dive

> Read `DETAILS.md` only when: changing batch reader config, upload pipeline, or factories.