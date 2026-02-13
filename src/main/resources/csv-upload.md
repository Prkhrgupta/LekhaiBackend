# Runtime Flow (Step-by-Step)

Job/Step kickoff — base class (CsvUploadService) or controller triggers the Batch Job and passes the CSV file resource.

Reader opens the file (FlatFileItemReader).

Header skipped (setLinesToSkip(1)), so the first line is ignored.

For each CSV line:

DelimitedLineTokenizer splits the line into tokens using the delimiter and quotes handling.

Tokens are assigned names brokerCode and brokerName (because of setNames).

DefaultLineMapper hands the FieldSet to BeanWrapperFieldSetMapper.

BeanWrapperFieldSetMapper instantiates BrokerDto (requires public no-arg constructor) and calls setBrokerCode(...) and setBrokerName(...) with converted values (String→Long conversion is attempted for brokerCode).

Processor converts BrokerDto → Broker entity.

Writer batches entities and calls repository to persist them.

Chunk commit behavior: items are committed in chunks — if an exception occurs, the chunk is retried/rolled back per configured retry/fault-tolerance.

---

# Important Attributes

delimitedLineTokenizer.setNames(...) — these must match your DTO property names (brokerCode → setBrokerCode(...)). If they don’t match, binding fails or behaves unexpectedly.

setStrict(true) — will throw when token count doesn’t match the names length; good for catching bad CSV lines early.

DTO requirements for BeanWrapperFieldSetMapper:

- public no-arg constructor,
- public setters for each property to be bound,
- property types compatible with CSV values (or a converter available).

fileItemReader.setLinesToSkip(1) — skip header; if you don’t skip headers and the tokenizer expects numeric values, you’ll get conversion errors.

Quoting & delimiter — DelimitedLineTokenizer handles quotes by default; if your CSV uses a different delimiter (tab, ;) set it with setDelimiter(...).

Type conversion — String → Long conversion is attempted based on the setter parameter type. Bad values will cause parsing errors.

Repository writer — ensure brokerRepository is the correct Spring Data repository and that saving a Broker works as expected (e.g., cascade, constraints).
