# csv — details

## Files & Roles

| File | Role |
|---|---|
| `upload/controller/CsvUploadController.java` | Multipart CSV upload endpoint; validates type; delegates to factory. |
| `upload/model/CsvUploadTypes.java` | Enum `TRANSPORT, AREA, BROKER, LEDGER` mapping to service classes. |
| `upload/factory/CsvUploadFactory.java` | Builds `Map<CsvUploadTypes, CsvUploadService>` from injected bean implementations (strategy lookup). |
| `upload/service/CsvUploadService.java` | `interface CsvUploadService<T,D>` — parse rows `T`, map to domain `D`, persist. |
| `upload/service/implementations/Upload*Csv.java` | One service per type; OpenCSV `@CsvBindByName` on row DTOs; row-level validation + dedupe. |
| `upload/service/OpenCsvItemReader.java` | `ItemReader` wrapping OpenCSV for Spring Batch chunks. |
| `upload/dto/AreaCsvDto.java` etc. | CSV row models with `@CsvBindByName`. |

## Behavioral Notes

- Every persisted row must carry shop scoping — upload services run within shop context (consumer `ShopAwareEntity` via `common/domain`).
- Strategy pattern: adding impls auto-registers; keep enum + bean name conventions aligned.
- Batch jobs use Spring Batch tables (`V11__create_spring_batch_table.sql`). Chunk size/commit policy is config-driven.
- Parse errors should be collected per row and surfaced via the `error/` envelope, not abort-run-on-first-error (unless intended).