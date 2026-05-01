package in.lekhai.category.transporter.controller;

import in.lekhai.category.transporter.TransporterScheduler;
import in.lekhai.category.transporter.service.TransporterService;
import in.lekhai.contract.api.TransporterEwbApi;
import in.lekhai.contract.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
public class TransporterEwbController implements TransporterEwbApi{
    private final TransporterService transporterService;
    private final TransporterScheduler transporterScheduler;
    private final ZoneId IST = ZoneId.of(ZoneId.SHORT_IDS.get("IST"));

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public TransporterEwbController(TransporterService transporterService,
                                    TransporterScheduler transporterScheduler) {
        this.transporterService = transporterService;
        this.transporterScheduler = transporterScheduler;
    }


    @Override
    public ResponseEntity<EwbExtendResponse> extendEwbValidity(@NotNull String ewbNo,
                                                               @Valid EwbExtendRequest ewbExtendRequest) {
        log.info("Called received to extend EWB with no : {}", ewbNo);
        return null;
    }

    @Override
    public ResponseEntity<EwbDetails> getEwbDetails(@NotNull String ewbNo) {
        EwbDetails response = transporterService.ewbDetailsByNo(ewbNo);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<EwbSummary>> getEwbExpiring(@NotNull @Valid OffsetDateTime offsetDateTime) {
        List<EwbSummary> response = transporterService.getEwbExpiringTill(offsetDateTime.toInstant());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<EwbSummary>> getTransporterEwbs(@NotNull @Valid LocalDate fromDate,
                                                               @NotNull @Valid LocalDate toDate,
                                                               @Valid Boolean includeDelivered,
                                                               @Valid EwbStatus ewbStatus) {
        List<EwbSummary> response = transporterService.getEwbsForTransporterByDate(
                fromDate.atStartOfDay(IST).toInstant(),
                toDate.plusDays(1).atStartOfDay(IST).toInstant(),
                includeDelivered != null && includeDelivered,
                ewbStatus);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> markEwbDelivered(@NotNull String ewbNo) {
        transporterService.setDeliveredStatus(ewbNo, true);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @Override
    public ResponseEntity<Void> markEwbNonDelivered(@NotNull String ewbNo) {
        transporterService.setDeliveredStatus(ewbNo, false);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }


    @RequestMapping(
            method = {RequestMethod.POST},
            value = {"/transporter/ewb/extend"}
    )
    public ResponseEntity<Void> reloadEwb(OffsetDateTime dateTime) {
        transporterScheduler.realoadEwbForDate(dateTime.toInstant());
        return ResponseEntity.ok(null);
    }
}
