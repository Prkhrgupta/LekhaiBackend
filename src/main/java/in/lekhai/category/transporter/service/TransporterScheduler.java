package in.lekhai.category.transporter.service;

import in.lekhai.category.transporter.util.TransporterMapper;
import in.lekhai.contract.model.Day;
import in.lekhai.contract.model.EwbExtendRequest;
import in.lekhai.contract.model.EwbSummary;
import in.lekhai.contract.model.ExtensionReason;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Component
public class TransporterScheduler {
    @Value("${features-flag.ewb-scheduler}")
    private final Set<Integer> ewbSchedulerShopCodes;
    private final TransporterService transporterService;
    private final ShopsRepo shopsRepo;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TransporterScheduler(Set<Integer> ewbSchedulerShopCodes,
                                TransporterService transporterService,
                                TransporterMapper transporterMapper,
                                ShopsRepo shopsRepo
    ) {
        this.ewbSchedulerShopCodes = ewbSchedulerShopCodes;
        this.transporterService = transporterService;
        this.shopsRepo = shopsRepo;
    }


    @Scheduled(cron = "0 45 2 * * *", zone = "Asia/Kolkata")
    //TODO : add a failed queue if the automatic fetch fails
    public void fetchEwbsForTransporterAtMidnight() {
        for(Integer shopCode : ewbSchedulerShopCodes) {
            try {
                log.info("Start to fetch Ewbs for shopCode {}", shopCode);
                ShopContext.setShopCode(shopCode);

                Shops shopDetails = shopsRepo.findByShopCode(shopCode)
                        .orElseThrow(() -> new IllegalStateException("Shop not found for shopCode " + shopCode));
                Instant now = Instant.now().minus(1, ChronoUnit.DAYS);
                transporterService.saveAllEwbForTransporterForDate(shopDetails.getGstNumber(), now, shopCode);
            } catch (Exception e) {
                log.error("Failed to fetch EWBs for shopCode {}", shopCode, e);
            } finally {
                ShopContext.clear();
            }
        }
    }

    // every day 2:40 AM cron >> extend validity for expiring ewb
    @Scheduled(cron = "0 40 2 * * *", zone = "Asia/Kolkata")
    public void extendValidityCron() {
        for(var shopCode : ewbSchedulerShopCodes) {
            try {
                log.info("Extending validity for  shop {} at {} ", shopCode, LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                ShopContext.setShopCode(shopCode);

                List<EwbSummary> ewbExpiring = transporterService.getEwbExpiringOn(Day.TODAY, false);
                for(var ewbSummary : ewbExpiring) {
                    try {
                        transporterService.extendEwbValidity(ewbSummary.getEwbNo(), buildEwbExtendRequest(ewbSummary));
                    } catch (Exception e) {
                        log.error("Failed to Auto-extend ewbNo=[{}]", ewbSummary.getEwbNo(), e);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to extend validity for shop {}", shopCode, e);
            } finally {
                ShopContext.clear();
            }
        }
    }

    private EwbExtendRequest buildEwbExtendRequest(EwbSummary ewbSummary) {
        return new EwbExtendRequest()
                .remainingDistance(ewbSummary.getActualDistance())
                .extensionReason(ExtensionReason.OTHERS)
                .extensionRemark("unloading issue");
    }
}
