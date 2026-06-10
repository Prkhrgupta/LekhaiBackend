package in.lekhai.gsp.ewb.repository.service;

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

@Service
public class EwbRecordRepoService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final EwbRecordRepo ewbRecordRepo;
    private final ShopsRepo shopsRepo;

    public EwbRecordRepoService(EwbRecordRepo ewbRecordRepo,
                                ShopsRepo shopsRepo) {
        this.ewbRecordRepo = ewbRecordRepo;
        this.shopsRepo = shopsRepo;
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
}
