# category

The **transporter & E-way bill (EWB) desk** — the module that keeps goods moving legally. In Tally-adjacent terms, this is the "transport + E-way bill" workflow: when a consignment travels by road, the law requires a valid EWB, and this module makes sure it exists, stays valid, and is easy to record.

## Business Goal

Most of a trading firm's goods leave its books and travel through third-party carriers (**transporters**). Under GST rules, a consignment in transit above the threshold value must carry a valid **E-way bill** issued by the GST system. This module handles the transporter side of that obligation:

- **Record transporter moves** — for a transporter's consignment, create, validate and renew the EWB through the provider (delegating the actual GST-system call to `gsp/`, never calling TaxPro directly).
- **Keep EWBs valid automatically** — a scheduled job periodically refreshes/renews EWB validity, so goods already on the road aren't found with an expired bill.
- **Summarise for the office** — a flattened EWB summary that can be exported to Excel (e.g. for dispatch registers or transporter statements).

A transporter is also a party in the books (see `core/account_master/`), so this module works alongside the ledger/transport master rather than owning its own.

## Public API

- `transporter/controller/TransporterEwbController` — REST endpoints for transporter EWB operations (implements the generated contract).
- `transporter/service/TransporterService` — create/validate/renew EWB for transporter moves; delegates provider calls to `gsp/ewb`.
- `transporter/service/TransporterScheduler` — scheduled jobs that keep EWB validity refreshed/propagated.
- `transporter/util/TransporterMapper` — contract DTO ↔ transporter domain mapping.
- `transporter/model/EwbSummaryExportDTO` — flattened projection used for the Excel EWB summary export.

## Dependencies

- `gsp/` (EWB provider port — all GST-system calls live there), `core/account_master/` (transporter/ledger records), `common/`, `error/`.

## How to Extend

To add a transporter EWB flow (e.g. a new EWB maintenance action):

1. Add the endpoint in `TransporterEwbController` + logic in `TransporterService` (validate/renew/create), delegating wire calls to `gsp/`.
2. If it repeats periodically, add a job to `TransporterScheduler` (keep provider timeouts consistent with `gsp/shared/TaxProProperties`).
3. Extend `EwbSummaryExportDTO` + the Excel column set if the new flow must appear in the summary export.

## Deep Dive

> Read `DETAILS.md` when: changing scheduler cadence, EWB integration logic, or the export mapping.