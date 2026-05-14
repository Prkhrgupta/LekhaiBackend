package in.lekhai.category.transporter.service;

import in.lekhai.category.transporter.util.TransporterMapper;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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


    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Kolkata")
    //TODO : add a failed queue if the automatic fetch fails
    public void fetchEwbsForTransporterAtMidnight() {
        for(Integer shopCode : ewbSchedulerShopCodes) {
            log.info("Start to fetch Ewbs for shopCode {}", shopCode);
            ShopContext.setShopCode(shopCode);

            Optional<Shops> shopDetails = shopsRepo.findByShopCode(shopCode);
            if(shopDetails.isEmpty()) {
                throw new RuntimeException("Something went wrong");
            }
            String gstNumber = shopDetails.get().getGstNumber();
            Instant now = Instant.now().minus(1, ChronoUnit.DAYS);
            transporterService.saveAllEwbForTransporterForDate(gstNumber, now, shopCode);

            ShopContext.clear();
        }
    }
}
