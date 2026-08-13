package in.lekhai.voucher.service;

import in.lekhai.shop.context.model.ShopContext;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.entity.VoucherCounter;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.repository.VoucherCounterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class VoucherCounterService {
    private final VoucherCounterRepository voucherCounterRepository;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public VoucherCounterService(
            VoucherCounterRepository voucherCounterRepository
    ) {
        this.voucherCounterRepository = voucherCounterRepository;
    }

    // This method call will lock this row
    @ShopContextTransactional
    public VoucherCounter fetchNextVoucherCounter(VoucherType voucherType) {
        return voucherCounterRepository.findForUpdate(voucherType)
                .orElseGet(() -> initiateVoucherCounter(voucherType));
    }

    private VoucherCounter initiateVoucherCounter(VoucherType voucherType) {
        VoucherCounter initialCounter = new VoucherCounter(voucherType, 1L);
        voucherCounterRepository.save(initialCounter);
        log.info("Initiated [{}] voucher counter for shopCode=[{}]", voucherType, ShopContext.getShopCode());
        return initialCounter;
    }

    public void increaseVoucherCounter(VoucherCounter counter) {
        counter.setNextNumber(counter.getNextNumber() + 1);
        voucherCounterRepository.save(counter);
    }
}
