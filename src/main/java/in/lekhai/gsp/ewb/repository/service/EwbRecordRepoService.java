package in.lekhai.gsp.ewb.repository.service;

import in.lekhai.category.transporter.util.TransporterMapper;
import in.lekhai.contract.model.EwbSummary;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import in.lekhai.gsp.ewb.repository.EwbRecordRepo;
import in.lekhai.shop.context.model.ShopContext;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EwbRecordRepoService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final EwbRecordRepo ewbRecordRepo;
    private final ShopsRepo shopsRepo;
    private final TransporterMapper transporterMapper;

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    public EwbRecordRepoService(
            EwbRecordRepo ewbRecordRepo,
            ShopsRepo shopsRepo,
            TransporterMapper transporterMapper
    ) {
        this.ewbRecordRepo = ewbRecordRepo;
        this.shopsRepo = shopsRepo;
        this.transporterMapper = transporterMapper;
    }

    @ShopContextTransactional
    public void extendAndSaveEwbValidity(EwbRecord ewbRecord, ExtendValidity extendValidity) {
        ewbRecord.setValidUpTo(extendValidity.newValidUpTo());
        ewbRecordRepo.save(ewbRecord);
    }

    @ShopContextTransactional
    public @NonNull EwbRecord getEwbRecord(String ewbNo) {
        log.info("Fetching EWB record for shop {} : {}", ShopContext.getShopCode(), ewbNo);
        EwbRecord ewbRecord = ewbRecordRepo.findByEwbNo(ewbNo)
                .orElseThrow(() -> {
                    log.error("No EWB record found for shop {} and ewbNo {}", ShopContext.getShopCode(), ewbNo);
                    return new LekhaiClientException("No Ewb record found for ewbNo=[{}]", HttpStatus.BAD_REQUEST);
                });
        log.info("Successfully fetched EWB record for shop {} : {}", ShopContext.getShopCode(), ewbNo);
        return ewbRecord;
    }

    @ShopContextTransactional
    public String getGstNumberForShop() {
        Integer shopCode = ShopContext.getShopCode();
        Shops shopDetails = shopsRepo.findByShopCode(shopCode)
                .orElseThrow(() -> {
                    log.error("Shop=[{}] not found", shopCode);
                    return new RuntimeException(String.format("ShopCode : [%s] not found", shopCode));
                });
        return shopDetails.getGstNumber();
    }

    @ShopContextTransactional
    public List<EwbSummary> getExpiringEwbsOnDayIncludingDelivered(Instant start, Instant end) {
        log.info("Fetching expiring EWBs including delivered for shop {}", ShopContext.getShopCode());
        List<EwbSummary> expiringEwbs = ewbRecordRepo
                .findByValidUpToGreaterThanEqualAndValidUpToLessThan(start, end)
                .stream()
                .map(transporterMapper::ewbRecordToSummary)
                .toList();
        log.info("Total expiring EWBs including delivered for shop {} : {}",
                ShopContext.getShopCode(), expiringEwbs.size());
        return expiringEwbs;
    }

    @ShopContextTransactional
    public List<EwbSummary> getExpiringEwbsOnDayExcludingDelivered(Instant start, Instant end) {
        log.info("Fetching expiring EWBs excluding delivered for shop {}", ShopContext.getShopCode());
        List<EwbSummary> expiringEwbs = ewbRecordRepo
                .findByValidUpToGreaterThanEqualAndValidUpToLessThanAndDeliveredFalse(start, end)
                .stream()
                .map(transporterMapper::ewbRecordToSummary)
                .toList();
        log.info("Total expiring EWBs excluding delivered for shop {} : {}",
                ShopContext.getShopCode(), expiringEwbs.size());
        return expiringEwbs;
    }

    @ShopContextTransactional
    public List<Long> findNewEwbsByEwbNumbers(List<Long> ewbNoList) {
        log.info("Fetching Ewbs for shop {} for list of {}", ShopContext.getShopCode(), ewbNoList.size());
        Set<Long> existingEwbNos = ewbRecordRepo
                .findByEwbNoIn(ewbNoList)
                .stream()
                .map(EwbRecord::getEwbNo)
                .collect(Collectors.toSet());

        log.info("Successfully fetched Ewb numbers for shop {} :: {}", ShopContext.getShopCode(), ewbNoList.size());
        return ewbNoList.stream()
                .filter(ewbNo -> {
                    boolean isNew = !existingEwbNos.contains(ewbNo);
                    if(!isNew) log.info("Ewb number=[{}] exists in DB for shop {}", ewbNo, ShopContext.getShopCode());
                    return isNew;
                }).toList();
    }

    @ShopContextTransactional
    public void saveListOfEwbRecords(String gstin, List<EwbRecord> saveEwbRecords) {
        log.info("Saving list of Ewb records for shop {} :: {}", ShopContext.getShopCode(), saveEwbRecords.size());
        ewbRecordRepo.saveAll(saveEwbRecords);
        log.info("Saved [{}] ewb records for gstin=[{}] and shop=[{}]",
                saveEwbRecords.size(), gstin, ShopContext.getShopCode());
    }
}
