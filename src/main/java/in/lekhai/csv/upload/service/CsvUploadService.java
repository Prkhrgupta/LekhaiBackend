package in.lekhai.csv.upload.service;

import in.lekhai.csv.upload.model.CsvUploadTypes;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

public abstract class CsvUploadService<I, O> {

    protected final JobRepository jobRepository;
    protected final PlatformTransactionManager transactionManager;
    protected final JobLauncher jobLauncher;

    protected CsvUploadService(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JobLauncher jobLauncher
    ) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.jobLauncher = jobLauncher;
    }

    protected abstract ItemReader<I> createReader(FileSystemResource resource);

    protected abstract ItemProcessor<I, O> createProcessor();

    protected abstract ItemWriter<O> createWriter();

    protected int getChunkSize() { return 100; }

    public abstract CsvUploadTypes getType();

    public void processCsv(MultipartFile csvFile, CsvUploadTypes csvUploadTypes)
            throws IOException, JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException,
            JobParametersInvalidException, JobRestartException {

        // 1. persist to temp file
        File tmp = File.createTempFile("upload-" + csvUploadTypes.name() + "-", ".csv");
        csvFile.transferTo(tmp.toPath());

        // 2. build reader/processor/writer for this run
        FileSystemResource resource = new FileSystemResource(tmp);
        ItemReader<I> reader = createReader(resource);
        ItemProcessor<I, O> processor = createProcessor();
        ItemWriter<O> writer = createWriter();

        // 3. build step using StepBuilder
        String stepName = getType().name() + "ImportStep";
        Step step = new StepBuilder(stepName, jobRepository)
                .<I, O>chunk(getChunkSize(), transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .build();

        // 4. build job using JobBuilder
        String jobName = getType().name() + "ImportJob-" + Instant.now().toEpochMilli();
        Job job = new JobBuilder(jobName, jobRepository)
                .start(step)
                .build();

        // 5. run job with job parameters so Spring Batch knows it's unique
        var params = new JobParametersBuilder()
                .addString("filePath", tmp.getAbsolutePath())
                .addString("uploadType", csvUploadTypes.name())
                .addDate("startedAt", Date.from(Instant.now()))
                .toJobParameters();

        jobLauncher.run(job, params);
    }
}
