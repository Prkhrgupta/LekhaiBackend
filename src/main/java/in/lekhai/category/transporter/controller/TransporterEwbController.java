package in.lekhai.category.transporter.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.category.transporter.service.TransporterScheduler;
import in.lekhai.category.transporter.service.TransporterService;
import in.lekhai.contract.api.TransporterEwbApi;
import in.lekhai.contract.model.*;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.shop.context.model.ShopContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
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
    public ResponseEntity<Resource> downloadTransporterEwbs(
            @NotNull @Valid LocalDate fromDate,
            @NotNull @Valid LocalDate toDate,
            @Valid Format format
    ) {
        log.info("Got a request to download EWBS for shop {} :: date : {} to {}",
                ShopContext.getShopCode(), fromDate, toDate);
        if (format == null || format == Format.EXCEL) {
            ResponseEntity<Resource> response = transporterService.exportExcelForEwbSummary(
                    fromDate.atStartOfDay(IST).toInstant(),
                    toDate.plusDays(1).atStartOfDay(IST).toInstant()
            );
            log.info("Successfully exported EWBS for shop {} : {} :: date : {} to {}",
                    ShopContext.getShopCode(), format, fromDate, toDate);
            return response;
        } else {
            log.error("Invalid format to download EWBs :: {} : {}", ShopContext.getShopCode(), format);
            throw new LekhaiClientException(String.format("The format: %s is not available", format));
        }
    }

    @Override
    public ResponseEntity<EwbExtendResponse> extendEwbValidity(@NotNull String ewbNo,
                                                               @Valid EwbExtendRequest ewbExtendRequest) {
        log.info("Got a request to extend EWB validity for shop {} : {} :: {}",
                ShopContext.getShopCode(), ewbNo, ewbExtendRequest.getExtensionReason());
        EwbExtendResponse ewbResponse = transporterService.extendEwbValidity(ewbNo, ewbExtendRequest);
        log.info("Successfully extended EWB validaity for shop {} : {}", ShopContext.getShopCode(), ewbNo);
        return ResponseEntity.ok(ewbResponse);
    }

    @Override
    public ResponseEntity<EwbDetails> getEwbDetails(@NotNull String ewbNo) {
        log.info("Got a request to fetch EWB details for shop {} : {}", ShopContext.getShopCode(), ewbNo);
        EwbDetails response = transporterService.ewbDetailsByNo(ewbNo);
        log.info("Successfully fetched EWB details for shop {} : {}", ShopContext.getShopCode(), ewbNo);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<EwbSummary>> getEwbExpiring(@NotNull @Valid Day day,
                                                           @Valid Boolean includeDelivered) {
        log.info("Got a request to fetch expiring EWBs for shop {} :: {}", ShopContext.getShopCode(), day);
        List<EwbSummary> response;

        if(day.equals(Day.ALREADY_EXPIRED)) {
            response = transporterService.getAlreadyExpiredEwbs();
        } else {
            response = transporterService.getEwbExpiringOn(day, includeDelivered);
        }

        log.info("Successfully fetched all expiring EWBs for shop {} :: {}", ShopContext.getShopCode(), day);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<EwbSummary>> getTransporterEwbs(
            @NotNull @Valid LocalDate fromDate,
            @NotNull @Valid LocalDate toDate,
            @Valid Boolean includeDelivered,
            @Valid EwbStatus ewbStatus
    ) {
        log.info("Got a request to fetch EWBs for shop {} :: date : {} to {}",
                ShopContext.getShopCode(), fromDate, toDate);
        List<EwbSummary> response = transporterService.getEwbsForTransporterByDate(
                fromDate.atStartOfDay(IST).toInstant(),
                toDate.plusDays(1).atStartOfDay(IST).toInstant(),
                includeDelivered != null && includeDelivered,
                ewbStatus);
        log.info("Successfully fetched EWBs for shop {}", ShopContext.getShopCode());
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
            method = {RequestMethod.GET},
            value = {"/transporter/ewb/manual-trigger"}
    )
    public ResponseEntity<Void> reloadEwb() {
        transporterScheduler.fetchEwbsForTransporterAtMidnight();
        return ResponseEntity.ok(null);
    }
}
