package in.lekhai.csv.upload.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.csv.upload.factory.CsvUploadFactory;
import in.lekhai.csv.upload.model.CsvUploadTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class CsvUploadController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public final CsvUploadFactory csvUploadFactory;

    public CsvUploadController(
            CsvUploadFactory csvUploadFactory
    ) {
        this.csvUploadFactory = csvUploadFactory;
    }

    @PostMapping("/csv/upload")
    public ResponseEntity<Result<Void>> uploadDataViaCsv(
            @RequestParam("csv-file") MultipartFile csvFile,
            @RequestParam("upload-type") CsvUploadTypes csvUploadTypes
    ) throws IOException, JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        if(csvFile.isEmpty()) {
            log.error("Empty csv file uploaded {} for {}", csvFile.getOriginalFilename(), csvUploadTypes);
            return ResponseEntity.badRequest().body(Result.error("Empty CSV uploaded"));
        }
        if(!Objects.requireNonNull(csvFile.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
            log.error("Invalid csv format for {} for {}", csvFile.getOriginalFilename(), csvUploadTypes);
            return ResponseEntity.badRequest().body(Result.error("Invalid format, only .csv files are supported"));
        }
        log.info("Request to upload csv {} for {}", csvFile.getOriginalFilename(), csvUploadTypes);
        csvUploadFactory.get(csvUploadTypes).processCsv(csvFile, csvUploadTypes);
        log.info("Successfully upload csv {} for {}", csvFile.getOriginalFilename(), csvUploadTypes);
        return ResponseEntity.ok(Result.success(String.format("Successfully upload %s", csvFile.getOriginalFilename())));
    }
}
