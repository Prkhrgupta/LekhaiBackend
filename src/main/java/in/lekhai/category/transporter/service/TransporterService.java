package in.lekhai.category.transporter.service;

import in.lekhai.category.transporter.util.TransporterMapper;
import in.lekhai.contract.model.EwbExtendRequest;
import in.lekhai.contract.model.EwbExtendResponse;
import in.lekhai.contract.model.EwbStatus;
import in.lekhai.contract.model.EwbSummary;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.entity.EwbVehicleDetail;
import in.lekhai.gsp.ewb.domain.enums.ExtendValidityReason;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.model.EwbForTransporter;
import in.lekhai.gsp.ewb.domain.model.ExtendValidity;
import in.lekhai.gsp.ewb.domain.port.EwbProvider;
import in.lekhai.gsp.ewb.domain.repository.EwbRecordRepo;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransporterService {
    private final EwbRecordRepo ewbRecordRepo;
    private final TransporterMapper transporterMapper;
    private final EwbProvider ewbProvider;
    private final ShopsRepo shopsRepo;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TransporterService(EwbRecordRepo ewbRecordRepo,
                              TransporterMapper transporterMapper,
                              EwbProvider ewbProvider,
                              ShopsRepo shopsRepo) {
        this.ewbRecordRepo = ewbRecordRepo;
        this.transporterMapper = transporterMapper;
        this.ewbProvider = ewbProvider;
        this.shopsRepo = shopsRepo;
    }

    @ShopContextTransactional
    public List<EwbSummary> getEwbsForTransporterByDate(Instant fromDate,
                                                        Instant toDate,
                                                        boolean includeDelivered,
                                                        EwbStatus ewbStatus) {
        return ewbRecordRepo.findByEwayBillDateGreaterThanEqualAndEwayBillDateLessThan(fromDate, toDate)
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
    }

    @ShopContextTransactional
    public List<EwbSummary> getEwbExpiringTill(Instant dateTime) {
        return ewbRecordRepo.findByValidUpToLessThanEqualAndDeliveredFalse(dateTime)
                .stream()
                .map(transporterMapper::ewbRecordToSummary)
                .toList();
    }

    @ShopContextTransactional
    public in.lekhai.contract.model.EwbDetails ewbDetailsByNo(String ewbNo) {
        EwbRecord ewbRecord = ewbRecordRepo.findByEwbNo(ewbNo)
                .orElseThrow(() -> new LekhaiClientException("No Ewb record found for ewbNo=[{}]", HttpStatus.BAD_REQUEST));
        return transporterMapper.ewbRecordToContractEwbResponse(ewbRecord);
    }

    @ShopContextTransactional
    public void setDeliveredStatus(String ewbNo, boolean deliveryStatus) {
        Optional<EwbRecord> ewbRecordOpt = ewbRecordRepo.findByEwbNo(ewbNo);
        if(ewbRecordOpt.isEmpty()) {
            throw new RuntimeException("Invalid Ewb passed");
        }
        ewbRecordOpt.get().setDelivered(deliveryStatus);
        ewbRecordRepo.save(ewbRecordOpt.get());
    }

    @ShopContextTransactional
    public EwbExtendResponse extendEwbValidity(String ewbNo,
                                               EwbExtendRequest extendValidityRequest
    ) {
        log.info("Extend ewb request for ebwNo=[{}]", ewbNo);
        EwbRecord ewbRecord = ewbRecordRepo.findByEwbNo(ewbNo)
                .orElseThrow(() -> new RuntimeException(String.format("Invalid request to extend ewbNo : %s, Not present in DB", ewbNo)));
        Integer shopCode = JwtUtil.extractJwtClaim().shopCode();
        Optional<Shops> shopDetails = shopsRepo.findByShopCode(shopCode);
        if(shopDetails.isEmpty()) {
            throw new RuntimeException("Something went wrong");
        }
        String gstNumber = shopDetails.get().getGstNumber();
        ExtendValidity extendValidity = ewbProvider.extendValidity(ewbNo,
                extendValidityRequest.getRemainingDistance(),
                ExtendValidityReason.valueOf(extendValidityRequest.getExtensionReason().toString()),
                extendValidityRequest.getExtensionRemark(),
                gstNumber,
                shopCode);
        // save updated validUpTo to DB
        ewbRecord.setValidUpTo(extendValidity.newValidUpTo());
        ewbRecordRepo.save(ewbRecord);
        log.info("Successfully extended validity for EwbNo : {} and saved to DB", ewbNo);

        return transporterMapper.toEwbExtendResponse(extendValidity);
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

        for(Long ewbNo : ewbNoList) {
            if(existingEwbNos.contains(ewbNo)) {
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
