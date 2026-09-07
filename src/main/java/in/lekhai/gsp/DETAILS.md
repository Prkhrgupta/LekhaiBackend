# gsp — details

## Structure

```
controller/GspController          REST entry (EWB + GST endpoints)
ewb/
  config/EwbConfig                EWB feature config (e.g., provider selection, flags)
  domain/
    entity/                       EwbRecord, EwbVehicleDetail, GspUserCredentials
    enums/                        DocumentType, EwbStatus, EwbVehicleType,
                                  ExtendValidityReason, SubSupplyType, SupplyType,
                                  TransactionType, TransportMode
    model/                        EwbDetails, EwbForTransporter, ExtendValidity
    port/EwbProvider              ← the provider port
  infrastructure/taxpro/
    TaxProEwbProvider             adapter impl of EwbProvider
    client/   AuthTaxProClient,      EwbTaxProWebClient      (WebClient-based)
    config/   TaxProConfiguration   (beans: auth + ewb clients)
    dto/      TaxProAuthResponse, TaxProErrorResponse, TaxProEwbDetailResponse,
              TaxProEwbForTransporterResponse, TaxProExtendValidityRequest/Response
    exceptions/ TaxProUnauthorizedException
    mapper/   TaxProEwbMapper
    service/  TaxProAuthService   (token acquisition/caching)
    utils/    TaxProPojoUtils
  repository/ EwbRecordRepo, EwbVehicleDetailRepo, GspUserCredentialsRepo
  repository/service/ EwbRecordRepoService
  service/   GspCredentialService
gst/
  TaxProGstClient, dto/GstDetailsDto, dto/GstVerificationResponseDto, util/GstinUtils
shared/TaxProProperties            config props (base urls, timeouts, credentials env)
```

## Ports & Adapters Contract

- Port (`EwbProvider`) defines business operations; adapters (TaxPro) implement wire-level details.
- Selection via config (`provider.ewb: tax-pro`). New provider = new adapter bean, no controller changes.
- Domain models (`ewb/domain/model/*`) are provider-agnostic; mappers convert adapter DTOs (`infrastructure/**/dto/*`) into them.

## Behavioral Notes

- Auth tokens: `TaxProAuthService` acquires/caches; use `TaxProUnauthorizedException` to force refresh on 401.
- Credentials (`GspUserCredentials`) are per-shop; resolve via `GspCredentialService` inside shop context.
- Timeouts/retries come from `shared/TaxProProperties`; keep provider HTTP config there, not scattered.
- EWB records persist locally (`EwbRecordRepo`) even on provider success/failure for audit + scheduler resume.