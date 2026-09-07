# gsp

GST Suvidha Provider integrations (TaxPro): E-Way Bill (EWB) and GSTIN verification. Hexagonal "ports & adapters" style.

## Public API

- `controller/GspController` — GSP-facing REST endpoints.
- `ewb/domain/port/EwbProvider` — the provider **port** (interface). EWB operations: generate, cancel, extend validity, vehicle updates.
- `ewb/infrastructure/taxpro/TaxProEwbProvider` — TaxPro **adapter** implementing `EwbProvider`.
- `ewb/service/GspCredentialService` — GSP user credential management.
- `gst/TaxProGstClient` — GSTIN verification client.
- `ewb/repository/service/EwbRecordRepoService` — thin repo service over EWB records.
- Domain: `ewb/domain/entity/*` (`EwbRecord`, `EwbVehicleDetail`, `GspUserCredentials`), `ewb/domain/model/*`, `ewb/domain/enums/*` (8 enums).
- `shared/TaxProProperties` — provider config (base URLs, timeouts).

## Dependencies

- `common/`, `error/`, `shop/`, `category/` (transporter scheduler uses this), WebClient (WebFlux)

## How to Extend

- Add a new provider: implement `EwbProvider` (port) in a new `infrastructure/<provider>/` adapter, add beans + `provider.ewb` config switch.
- Add a GSP operation: extend `EwbProvider` port + controller, implement in adapters.

## Deep Dive

> Read `DETAILS.md` only when: modifying provider adapters, port contract, credentials handling, or GST client.