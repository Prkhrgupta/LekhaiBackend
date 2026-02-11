package in.lekhai.csv.upload.service.implementations;

import in.lekhai.csv.upload.model.CsvUploadTypes;
import in.lekhai.csv.upload.service.CsvUploadService;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

public class UploadLedgerCsv extends CsvUploadService {

    protected UploadLedgerCsv(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JobLauncher jobLauncher
    ) {
        super(jobRepository, transactionManager, jobLauncher);
    }

    @Override
    protected ItemReader createReader(FileSystemResource resource) {
        return null;
    }

    @Override
    protected ItemProcessor createProcessor() {
        return null;
    }

    @Override
    protected ItemWriter createWriter() {
        return null;
    }

    public CsvUploadTypes getType() {
        return CsvUploadTypes.LEDGER;
    }
}
