package in.lekhai.category.transporter;

import in.lekhai.category.transporter.service.TransporterService;
import in.lekhai.category.transporter.util.TransporterMapper;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.port.EwbProvider;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class TransporterScheduler {
    @Value("${features-flag.ewb-scheduler}")
    private final Set<Integer> ewbSchedulerShopCodes;
    private final EwbProvider ewbProvider;
    private final ShopsRepo shopsRepo;
    private final TransporterService transporterService;

    private final TransporterMapper transporterMapper;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public TransporterScheduler(Set<Integer> ewbSchedulerShopCodes,
                                EwbProvider ewbProvider,
                                ShopsRepo shopsRepo,
                                TransporterService transporterService,
                                TransporterMapper transporterMapper) {
        this.ewbSchedulerShopCodes = ewbSchedulerShopCodes;
        this.ewbProvider = ewbProvider;
        this.shopsRepo = shopsRepo;
        this.transporterService = transporterService;
        this.transporterMapper = transporterMapper;
    }


    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Kolkata")
    public void fetchEwbsForTransporterAtMidnight() {
        for(Integer shopCode : ewbSchedulerShopCodes) {
            ShopContext.setShopCode(shopCode);

            Optional<Shops> shopDetails = shopsRepo.findByShopCode(shopCode);
            if(shopDetails.isEmpty()) {
                throw new RuntimeException("Something went wrong");
            }
            String gstNumber = shopDetails.get().getGstNumber();
            Instant now = Instant.now();
            List<EwbRecord> ewbToBeCreated = ewbProvider.getEwbListForTransporter(gstNumber, now, shopCode)
                    .stream()
                    .map(transporterMapper::convertEwbForTransporterToEwbRecord)
                    .toList();

            transporterService.saveAllEwbRecord(ewbToBeCreated);

            ShopContext.clear();
            log.info("Successfully saved {} Ewb record for shopCode : {} at {}", ewbToBeCreated.size(), shopCode, now);
        }
    }

    public void realoadEwbForDate(Instant dateTime) {
        Integer shopCode = JwtUtil.extractJwtClaim().shopCode();
        Optional<Shops> shopDetails = shopsRepo.findByShopCode(shopCode);
        if(shopDetails.isEmpty()) {
            throw new RuntimeException("Something went wrong");
        }
        String gstNumber = shopDetails.get().getGstNumber();
        List<EwbRecord> ewbToBeCreated = ewbProvider.getEwbListForTransporter(gstNumber, dateTime, shopCode)
                .stream()
                .map(transporterMapper::convertEwbForTransporterToEwbRecord)
                .toList();

        transporterService.saveAllEwbRecord(ewbToBeCreated);
    }
}
