# category — details

## Files & Roles

| File | Role |
|---|---|
| `transporter/controller/TransporterEwbController.java` | REST entry for transporter EWB operations. |
| `transporter/service/TransporterService.java` | Creates/validates/renews EWB for transporter moves; delegates provider calls to `gsp/ewb`. |
| `transporter/service/TransporterScheduler.java` | `@Scheduled` jobs (e.g., periodic EWB validity refresh). |
| `transporter/util/TransporterMapper.java` | Mapping contract DTOs ↔ transporter domain objects. |
| `transporter/model/EwbSummaryExportDTO.java` | Flattened export projection for Excel. |

## Behavioral Notes

- `TransporterScheduler` is the "producer" of periodic EWB maintenance; coordinate changes with `gsp/` where the provider calls live. Do not call TaxPro clients directly from here.
- EWB validity handling is time-sensitive — keep UTC (`FinancialYearDateUtil` / `DateUtils` style) and provider timeouts consistent with `gsp/shared/TaxProProperties`.