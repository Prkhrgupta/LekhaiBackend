package in.lekhai.csv.upload.service.implementations;

import in.lekhai.core.account_master.domain.Area;
import in.lekhai.core.account_master.repository.AreaRepository;
import in.lekhai.csv.upload.dto.AreaCsvDto;
import in.lekhai.csv.upload.model.CsvUploadTypes;
import in.lekhai.csv.upload.service.CsvUploadService;
import in.lekhai.shop.context.transaction.manager.ShopContextTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class UploadAreaCsv extends CsvUploadService<AreaCsvDto, Area> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final AreaRepository areaRepository;

    protected UploadAreaCsv(
            JobRepository jobRepository,
            ShopContextTransactionManager transactionManager,
            JobLauncher jobLauncher,
            AreaRepository areaRepository
    ) {
        super(jobRepository, transactionManager, jobLauncher);
        this.areaRepository = areaRepository;
    }

    @Override
    protected ItemReader<AreaCsvDto> createReader(FileSystemResource resource) {
        FlatFileItemReader<AreaCsvDto> fileItemReader = new FlatFileItemReader<>();
        fileItemReader.setResource(resource);
        fileItemReader.setLinesToSkip(1);
        DefaultLineMapper<AreaCsvDto> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
        delimitedLineTokenizer.setNames("code", "name");
        delimitedLineTokenizer.setStrict(false);

        BeanWrapperFieldSetMapper<AreaCsvDto> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        beanWrapperFieldSetMapper.setTargetType(AreaCsvDto.class);

        lineMapper.setLineTokenizer(delimitedLineTokenizer);
        lineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);
        fileItemReader.setLineMapper(lineMapper);
        return fileItemReader;
    }

    @Override
    protected ItemProcessor<AreaCsvDto, Area> createProcessor() {
        return dto -> new Area(dto.getAreaName(), null);
    }

    @Override
    protected ItemWriter<Area> createWriter() {
        RepositoryItemWriter<Area> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(areaRepository);
        return repositoryItemWriter;
    }

    public CsvUploadTypes getType() {
        return CsvUploadTypes.AREA;
    }
}
