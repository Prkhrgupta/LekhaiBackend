package in.lekhai.csv.upload.service.implementations;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.core.account_master.domain.*;
import in.lekhai.core.account_master.repository.*;
import in.lekhai.csv.upload.dto.LedgerCsvDto;
import in.lekhai.csv.upload.model.CsvUploadTypes;
import in.lekhai.csv.upload.service.CsvUploadService;
import in.lekhai.csv.upload.service.OpenCsvItemReader;
import in.lekhai.shop.context.transaction.manager.ShopContextTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
public class UploadLedgerCsv extends CsvUploadService<LedgerCsvDto, Ledger> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final LedgerRepository ledgerRepository;
    private final AreaRepository areaRepository;
    private final BrokerRepository brokerRepository;
    private final TransportRepository transportRepository;
    private final AccountGroupRepository accountGroupRepository;

    private final Set<String> seenGstNumbers = new HashSet<>();

    protected UploadLedgerCsv(
            JobRepository jobRepository,
            ShopContextTransactionManager transactionManager,
            JobLauncher jobLauncher,
            LedgerRepository ledgerRepository,
            AreaRepository areaRepository,
            BrokerRepository brokerRepository,
            TransportRepository transportRepository,
            AccountGroupRepository accountGroupRepository
    ) {
        super(jobRepository, transactionManager, jobLauncher);
        this.ledgerRepository = ledgerRepository;
        this.areaRepository = areaRepository;
        this.brokerRepository = brokerRepository;
        this.transportRepository = transportRepository;
        this.accountGroupRepository = accountGroupRepository;
    }

    @Override
    protected ItemReader<LedgerCsvDto> createReader(FileSystemResource resource) {
        try {
            return new OpenCsvItemReader<>(resource, LedgerCsvDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create CSV reader", e);
        }
    }
    @Override
    protected ItemProcessor<LedgerCsvDto, Ledger> createProcessor() {
        return dto -> {
            String accountGroupName = dto.getAccountGroupName();
            Long accountGroupId = accountGroupRepository.findByNameIgnoreCase(accountGroupName)
                    .map(AccountGroup::getId)
                    .orElseThrow(() -> new RuntimeException(String.format("no account group found with name %s", accountGroupName)));

            Long brokerId = brokerRepository.findBySitswiftCode(dto.getBrokerCsvId())
                    .map(Broker::getId)
                    .orElse(null);

            Long areaId = areaRepository.findBySitswiftCode(dto.getAreaCsvId())
                    .map(Area::getId)
                    .orElse(null);

            Long transportId = transportRepository.findBySitswiftCode(dto.getTransportCsvId())
                    .map(Transport::getId)
                    .orElse(null);
            String aadharNo = null;
            String gstNo = normalize(dto.getGstNo());

            if(!dto.getAadharNo().isBlank() && dto.getAadharNo().length() <= 12) {
                aadharNo = dto.getAadharNo();
            }

            String pan = dto.getPanNo().isEmpty() ? null : dto.getPanNo();

            if (gstNo != null) {
                // check within file (same chunk + previous chunks)
                if (!seenGstNumbers.add(gstNo)) {
                    log.warn("Skipping duplicate GST in file: {}", gstNo);
                    return null;
                }

                // check DB
                if (ledgerRepository.findByGstInNumber(gstNo).isPresent()) {
                    log.warn("Skipping duplicate GST in DB: {}", gstNo);
                    return null;
                }
            }

            return new Ledger(
                    dto.getName(),
                    dto.getLegalName(),
                    accountGroupId,
                    BigDecimal.valueOf(dto.getOpeningBalance()),
                    dto.getOpeningBalance() > 0 ? AccountEntryType.CR : AccountEntryType.DR,
                    null,
                    areaId,
                    brokerId,
                    transportId,
                    pan,
                    aadharNo,
                    null,
                    null,
                    null,
                    null,
                    null,
                    gstNo,
                    dto.getLocation()
            );
        };
    }

    @Override
    protected ItemWriter<Ledger> createWriter() {
        RepositoryItemWriter<Ledger> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(ledgerRepository);
        return repositoryItemWriter;
    }

    public CsvUploadTypes getType() {
        return CsvUploadTypes.LEDGER;
    }

    private String normalize(String gst) {
        if (gst == null || gst.isBlank()) return null;
        return gst.trim().toUpperCase();
    }
}
