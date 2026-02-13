package in.lekhai.csv.upload.service.implementations;

import in.lekhai.core.account_master.domain.Broker;
import in.lekhai.core.account_master.repository.BrokerRepository;
import in.lekhai.csv.upload.dto.BrokerDto;
import in.lekhai.csv.upload.model.CsvUploadTypes;
import in.lekhai.csv.upload.service.CsvUploadService;
import in.lekhai.shop.context.transaction.manager.ShopContextTransactionManager;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class UploadBrokerCsv extends CsvUploadService<BrokerDto, Broker> {

    private final BrokerRepository brokerRepository;

    protected UploadBrokerCsv(
            JobRepository jobRepository,
            ShopContextTransactionManager transactionManager,
            JobLauncher jobLauncher,
            BrokerRepository brokerRepository
    ) {
        super(jobRepository, transactionManager, jobLauncher);
        this.brokerRepository = brokerRepository;
    }

    @Override
    protected ItemReader<BrokerDto> createReader(FileSystemResource resource) {
        FlatFileItemReader<BrokerDto> fileItemReader = new FlatFileItemReader<>();
        fileItemReader.setResource(resource);
        fileItemReader.setLinesToSkip(1);

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("brokerCode", "brokerName");
        delimitedLineTokenizer.setStrict(true);

        BeanWrapperFieldSetMapper<BrokerDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(BrokerDto.class);

        DefaultLineMapper<BrokerDto> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(delimitedLineTokenizer);
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        fileItemReader.setLineMapper(lineMapper);
        return fileItemReader;
    }

    @Override
    protected ItemProcessor<BrokerDto, Broker> createProcessor() {
        return dto -> new Broker(dto.getBrokerName(), null);
    }

    @Override
    protected ItemWriter<Broker> createWriter() {
        RepositoryItemWriter<Broker> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(brokerRepository);
        return repositoryItemWriter;
    }

    public CsvUploadTypes getType() {
        return CsvUploadTypes.BROKER;
    }
}
