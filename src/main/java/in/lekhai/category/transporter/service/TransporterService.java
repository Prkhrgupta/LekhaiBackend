package in.lekhai.category.transporter.service;

import in.lekhai.category.transporter.util.TransporterMapper;
import in.lekhai.contract.model.EwbStatus;
import in.lekhai.contract.model.EwbSummary;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.gsp.ewb.domain.entity.EwbRecord;
import in.lekhai.gsp.ewb.domain.model.EwbDetails;
import in.lekhai.gsp.ewb.domain.port.EwbProvider;
import in.lekhai.gsp.ewb.domain.repository.EwbRecordRepo;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        return ewbRecordRepo.findByEwbDateBetween(fromDate, toDate)
                .stream()
                .filter(ewb -> {
                    if(includeDelivered) {
                        return ewb.isDelivered();
                    }
                    if(Objects.nonNull(ewbStatus)) {
                        return ewbStatus.equals(ewb.getStatus().getEwbSummaryStatus());
                    }
                    return true;
                })
                .map(transporterMapper::ewbRecordToSummary)
                .toList();
    }

    @ShopContextTransactional
    public List<EwbSummary> getEwbExpiringTill(Instant dateTime) {
        return ewbRecordRepo.findByValidUpToLessThanEqualAndIsDeliveredFalse(dateTime)
                .stream()
                .map(transporterMapper::ewbRecordToSummary)
                .toList();
    }

    public in.lekhai.contract.model.EwbDetails ewbDetailsByNo(String ewbNo) {
        Integer shopCode = JwtUtil.extractJwtClaim().shopCode();
        Optional<Shops> shopDetails = shopsRepo.findByShopCode(shopCode);
        if(shopDetails.isEmpty()) {
            throw new RuntimeException("Something went wrong");
        }
        String gstNumber = shopDetails.get().getGstNumber();
        EwbDetails ewbDetails = ewbProvider.getEwbDetails(ewbNo, gstNumber, shopCode);
        return transporterMapper.ewbDetailsToContractEwbResponse(ewbDetails);
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
    public void saveAllEwbRecord(List<EwbRecord> ewbToBeCreated) {
        ewbRecordRepo.saveAll(ewbToBeCreated);
    }
}
