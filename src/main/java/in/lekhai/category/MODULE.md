# category

Transporter domain: E-way bill (EWB) scheduling for transporters and related export utilities.

## Public API

- `transporter/controller/TransporterEwbController` — EWB endpoints for transporters.
- `transporter/service/TransporterService` — EWB business logic for transporter records.
- `transporter/service/TransporterScheduler` — scheduled EWB refresh/push jobs.
- `transporter/util/TransporterMapper` — DTO ↔ entity mapping.
- `transporter/model/EwbSummaryExportDTO` — Excel export model for EWB summary.

## Dependencies

- `error/`, `common/`, `core/account_master/` (transporter/ledger records), `gsp/` (EWB provider)

## How to Extend

- Add a transporter EWB flow: extend `TransporterEwbController` + `TransporterService`; add export columns via `EwbSummaryExportDTO`.

## Deep Dive

> Read `DETAILS.md` only when: changing scheduler cadence, EWB integration logic, or export mapping.