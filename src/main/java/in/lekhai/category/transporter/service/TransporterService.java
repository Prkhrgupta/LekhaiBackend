package in.lekhai.category.transporter.service;

import in.lekhai.category.transporter.model.EwbSummaryExportDTO;
import in.lekhai.category.transporter.util.TransporterMapper;
import in.lekhai.common.excel.ExcelExporter;
import in.lekhai.contract.model.*;
import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.entity.EwbVehicleDetail;
import in.lekhai.gsp.ewb.domain.enums.ExtendValidityReason;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import in.lekhai.gsp.ewb.domain.port.EwbProvider;
import in.lekhai.gsp.ewb.repository.EwbRecordRepo;
import in.lekhai.gsp.ewb.repository.service.EwbRecordRepoService;
import in.lekhai.shop.context.model.ShopContext;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransporterService {
    private final EwbRecordRepo ewbRecordRepo;
    private final EwbRecordRepoService ewbRecordRepoService;
    private final TransporterMapper transporterMapper;
    private final EwbProvider ewbProvider;
    private final ExcelExporter excelExporter;

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public TransporterService(
            EwbRecordRepo ewbRecordRepo,
            EwbRecordRepoService ewbRecordRepoService,
            TransporterMapper transporterMapper,
            EwbProvider ewbProvider,
            ExcelExporter excelExporter
    ) {
        this.ewbRecordRepo = ewbRecordRepo;
        this.ewbRecordRepoService = ewbRecordRepoService;
        this.transporterMapper = transporterMapper;
        this.ewbProvider = ewbProvider;
        this.excelExporter = excelExporter;
    }

    @ShopContextTransactional
    public List<EwbSummary> getEwbsForTransporterByDate(
            Instant fromDate,
            Instant toDate,
            boolean includeDelivered,
            EwbStatus ewbStatus
    ) {
        log.info("Fetching EWB records for shop {} :: date : {} to {}", ShopContext.getShopCode(), fromDate, toDate);
        List<EwbSummary> ewbRecords = ewbRecordRepo
                .findByEwayBillDateGreaterThanEqualAndEwayBillDateLessThan(fromDate, toDate)
                .stream()
                .filter(ewb -> {
                    if (includeDelivered) return ewb.isDelivered();

                    if (Objects.nonNull(ewbStatus)) {
                        return ewbStatus.equals(ewb.getStatus().getEwbSummaryStatus());
                    }
                    return true;
                })
                .sorted(
                        Comparator.comparing(EwbRecord::getEwayBillDate).reversed()
                                .thenComparing(EwbRecord::getEwbNo, Comparator.reverseOrder())
                )
                .map(transporterMapper::ewbRecordToSummary)
                .toList();
        log.info("Successfully fetched EWB {} records for shop {} :: date {} : {}",
                ewbRecords.size(), ShopContext.getShopCode(), fromDate, toDate);
        return ewbRecords;
    }

    @ShopContextTransactional
    public List<EwbSummary> getEwbExpiringOn(Day day, boolean includeDelivered) {
        LocalDate targetDate = switch (day) {
            case TODAY -> LocalDate.now(IST);
            case TOMORROW -> LocalDate.now(IST).plusDays(1);
            default -> throw new IllegalArgumentException("Unexpected day: " + day);
        };

        Instant start = targetDate.atStartOfDay(IST).toInstant();
        Instant end = targetDate.plusDays(1).atStartOfDay(IST).toInstant();

        log.info("Fetching EWBs expiring {} for shop {}", day, ShopContext.getShopCode());
        List<EwbRecord> expiringEwbs;

        if(includeDelivered) {
            expiringEwbs = ewbRecordRepo.findByValidUpToGreaterThanEqualAndValidUpToLessThan(start, end);
        } else {
            expiringEwbs = ewbRecordRepo.findByValidUpToGreaterThanEqualAndValidUpToLessThanAndDeliveredFalse(start, end);
        }

        List<EwbSummary> ewbRecords = expiringEwbs
                .stream()
                .map(transporterMapper::ewbRecordToSummary)
                .toList();

        log.info("Total EWBs expiring {} for shop {} : {}", day, ShopContext.getShopCode(), ewbRecords.size());
        return ewbRecords;
    }

    public List<EwbSummary> getAlreadyExpiredEwbs() {
        Instant start = LocalDate.now(IST).minusDays(7).atStartOfDay(IST).toInstant();
        Instant endOfToday = LocalDate.now(IST).plusDays(1).atStartOfDay(IST).toInstant();
        log.info("Fetching already expired EWBs for shop {}", ShopContext.getShopCode());
        List<EwbSummary> expiredEwbs = ewbRecordRepo
                .findByValidUpToGreaterThanEqualAndValidUpToLessThanAndDeliveredFalse(start, endOfToday)
                .stream()
                .map(transporterMapper::ewbRecordToSummary)
                .toList();
        log.info("Total expired EWBs for shop {} : {}", ShopContext.getShopCode(), expiredEwbs.size());
        return expiredEwbs;
    }

    public in.lekhai.contract.model.EwbDetails ewbDetailsByNo(String ewbNo) {
        EwbRecord ewbRecord = ewbRecordRepoService.getEwbRecord(ewbNo);
        return transporterMapper.ewbRecordToContractEwbResponse(ewbRecord);
    }

    @ShopContextTransactional
    public void setDeliveredStatus(String ewbNo, boolean deliveryStatus) {
        EwbRecord ewbRecord = ewbRecordRepoService.getEwbRecord(ewbNo);
        ewbRecord.setDelivered(deliveryStatus);
        log.info("Saving EWB with delivered status as {} for shop {} : {}", deliveryStatus, ShopContext.getShopCode(), ewbNo);
        ewbRecordRepo.save(ewbRecord);
        log.info("Saved EWB with delivered status as {} for shop {} : {}", deliveryStatus, ShopContext.getShopCode(), ewbNo);
    }

    public EwbExtendResponse extendEwbValidity(String ewbNo,
                                               EwbExtendRequest extendValidityRequest
    ) {
        log.info("Extend ewb request for ebwNo=[{}]", ewbNo);
        EwbRecord ewbRecord = ewbRecordRepoService.getEwbRecord(ewbNo);
        String gstNumber = ewbRecordRepoService.getGstNumberForShop();
        Integer shopCode = ShopContext.getShopCode();
        ExtendValidity extendValidity = ewbProvider.extendValidity(
                ewbNo,
                extendValidityRequest.getRemainingDistance(),
                ExtendValidityReason.valueOf(extendValidityRequest.getExtensionReason().toString()),
                extendValidityRequest.getExtensionRemark(),
                gstNumber,
                shopCode
        );
        ewbRecordRepoService.extendAndSaveEwbValidity(ewbRecord, extendValidity);
        log.info("Successfully extended validity for EwbNo : {} and saved to DB", ewbNo);

        return transporterMapper.toEwbExtendResponse(extendValidity);
    }

    @ShopContextTransactional
    public ResponseEntity<Resource> exportExcelForEwbSummary(
            Instant fromDate,
            Instant toDate
    ) {
        List<EwbSummary> summaries = getEwbsForTransporterByDate(fromDate, toDate, false, null);
        List<EwbSummaryExportDTO> dtos = summaries
                .stream()
                .map(EwbSummaryExportDTO::new)
                .toList();

        byte[] bytes = excelExporter.export(dtos, EwbSummaryExportDTO.class);

        String filename = "EwbSummary_%s_to_%s.xlsx"
                .formatted(fromDate, toDate)
                .replace(":", "-");

        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""
                )
                .contentLength(bytes.length)
                .body(resource);
    }
    @ShopContextTransactional
    public void saveAllEwbForTransporterForDate(String gstin,
                                                Instant date,
                                                Integer shopCode) {
        List<Long> ewbNoList = ewbProvider.getEwbListForTransporter(gstin, date, shopCode)
                .stream()
                .map(EwbForTransporter::getEwbNo)
                .toList();
        Set<Long> existingEwbNos = ewbRecordRepo.findByEwbNoIn(ewbNoList)
                .stream()
                .map(EwbRecord::getEwbNo)
                .collect(Collectors.toSet());

        log.info("Total no of Ewbs to be saved for shopCode=[{}] are [{}]", shopCode, ewbNoList.size());
        List<EwbRecord> ewbRecordsToBeCreated = new ArrayList<>();

        for (Long ewbNo : ewbNoList) {
            if (existingEwbNos.contains(ewbNo)) {
                log.info("EwbNo=[{}] already exists in the DB for date=[{}]", ewbNo, date);
                continue;
            }
            EwbDetails ewbDetails = ewbProvider.getEwbDetails(ewbNo, gstin, shopCode);
            EwbRecord ewbRecord = transporterMapper.convertEwbDetailToEwbRecord(ewbDetails);
            EwbVehicleDetail ewbVehicleDetail =
                    transporterMapper.convertEwbDetailToEwbVehicle(
                            ewbDetails.ewbVehicleDetails().getFirst()
                    );
            ewbRecord.getVehicleDetailSet().add(ewbVehicleDetail);
            ewbRecordsToBeCreated.add(ewbRecord);
        }
        List<EwbRecord> savedEwbRecords = ewbRecordRepo.saveAll(ewbRecordsToBeCreated);
        log.info("Saved [{}] ewb records for gstin=[{}] and shopCode=[{}]", savedEwbRecords.size(), gstin, shopCode);
    }
}
