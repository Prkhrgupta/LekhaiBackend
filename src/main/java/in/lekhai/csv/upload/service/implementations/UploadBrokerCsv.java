package in.lekhai.csv.upload.service.implementations;

import in.lekhai.csv.upload.model.CsvUploadTypes;
import in.lekhai.csv.upload.service.CsvUploadService;
import in.lekhai.shop.context.transaction.manager.ShopContextTransactionManager;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.FileSystemResource;

public class UploadBrokerCsv extends CsvUploadService {

    protected UploadBrokerCsv(
            JobRepository jobRepository,
            ShopContextTransactionManager transactionManager,
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
        return CsvUploadTypes.BROKER;
    }
}
