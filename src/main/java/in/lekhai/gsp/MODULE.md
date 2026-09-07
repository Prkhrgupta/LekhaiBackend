# gsp

The **GST compliance** layer — where Lekhai talks to the government's GST systems through an authorised GST Suvidha Provider (GSP, here TaxPro). This is what makes a shop's tax paperwork legally sound: verifying that business partners are registered for GST, and generating the transit documents the law requires when goods move.

## Business Goal

Indian MSMEs must be sure the parties they bill are registered under GST, and goods in transit above the threshold need a valid E-way bill. This module delivers both, on the firm's behalf:

- **GSTIN verification** — before recording a party or issuing them an invoice, confirm that their GSTIN is real and active on the GSTN, so the firm doesn't claim input credit against a bogus number. Uses `TaxProGstClient` (`gst/`).
- **E-Way Bill (EWB) management** — a consignment moving between addresses above the value limit must carry an EWB (a document issued by the GST system). This module generates it, **cancels** it when a sale fails, **extends its validity** when transit takes longer, and **updates vehicle details** when goods are trans-shipped. It captures the fields the law cares about — supply type, transaction type, transport mode, document type, sub-supply — so the EWB is always filed correctly (`ewb/`).

The design is **hexagonal**: an `EwbProvider` port defines the business operations; the TaxPro integration is one *adapter* (`ewb/infrastructure/taxpro/`). The firm's choice of GSP is configurable, so a new provider can be slotted in without touching the rest of the system. EWB records are persisted locally (even on provider failure) so every generation attempt is auditable and schedulers can resume where they left off.

## Public API

- `controller/GspController` — REST endpoints for EWB and GST operations (implements the generated contract).
- `ewb/domain/port/EwbProvider` — the provider **port**: generate, cancel, extend validity, update vehicle details.
- `ewb/infrastructure/taxpro/TaxProEwbProvider` — the TaxPro **adapter** implementing that port (WebClient-based, auth token acquisition/caching in `TaxProAuthService`).
- `ewb/service/GspCredentialService` + `GspUserCredentials` — each shop's own GSP login credentials, resolved inside the shop context.
- `ewb/repository/service/EwbRecordRepoService` + `EwbRecord`/`EwbVehicleDetail` entities — the local audit trail of every EWB attempt.
- `ewb/domain/enums/*` — the GST-compliant vocabularies (supply type, transaction type, transport mode, document type, EWB status, extend-validity reason, vehicle type).
- `gst/TaxProGstClient` — GSTIN verification client + `GstinUtils` validators.
- `shared/TaxProProperties` — provider config (base URLs, timeouts, credential env). Keep provider HTTP config here, not scattered.

## Dependencies

- `common/`, `error/`, `shop/` (per-shop credentials + context), `category/` (the transporter scheduler triggers EWB flows here), WebClient (WebFlux).

## How to Extend

- **New GSP provider**: implement `EwbProvider` (port) in a new `ewb/infrastructure/<provider>/` adapter, add its beans, and switch via `provider.ewb` config — no controller changes.
- **New GST operation**: extend the `EwbProvider` port + `GspController`, implement it in each adapter, and map provider DTOs (`infrastructure/**/dto/*`) to provider-agnostic domain models (`ewb/domain/model/*`).

## Deep Dive

> Read `DETAILS.md` when: modifying the port contract, provider adapters, credentials handling, the GST client, or the local EWB audit/recovery behaviour.