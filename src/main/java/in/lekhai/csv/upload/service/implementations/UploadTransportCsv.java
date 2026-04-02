package in.lekhai.csv.upload.service.implementations;

import in.lekhai.core.account_master.domain.Transport;
import in.lekhai.core.account_master.repository.TransportRepository;
import in.lekhai.csv.upload.dto.TransportDto;
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
public class UploadTransportCsv extends CsvUploadService<TransportDto, Transport> {

    private final TransportRepository transportRepository;

    protected UploadTransportCsv(
            JobRepository jobRepository,
            ShopContextTransactionManager transactionManager,
            JobLauncher jobLauncher,
            TransportRepository transportRepository
    ) {
        super(jobRepository, transactionManager, jobLauncher);
        this.transportRepository = transportRepository;
    }

    @Override
    protected ItemReader<TransportDto> createReader(FileSystemResource resource) {
        FlatFileItemReader<TransportDto> fileItemReader = new FlatFileItemReader<>();
        fileItemReader.setResource(resource);
        fileItemReader.setLinesToSkip(1);

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("transportCode", "transportName", "transportAddress", "gstNumber");
        delimitedLineTokenizer.setStrict(true);

        BeanWrapperFieldSetMapper<TransportDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(TransportDto.class);

        DefaultLineMapper<TransportDto> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(delimitedLineTokenizer);
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        fileItemReader.setLineMapper(lineMapper);
        return fileItemReader;
    }

    @Override
    protected ItemProcessor<TransportDto, Transport> createProcessor() {
        return dto -> new Transport(dto.getTransportName(), dto.getGstNumber(), dto.getTransportCode());
    }

    @Override
    protected ItemWriter<Transport> createWriter() {
        RepositoryItemWriter<Transport> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(transportRepository);
        return repositoryItemWriter;
    }

    public CsvUploadTypes getType() {
        return CsvUploadTypes.TRANSPORT;
    }
}
