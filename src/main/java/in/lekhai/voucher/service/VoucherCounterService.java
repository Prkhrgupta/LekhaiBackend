package in.lekhai.voucher.service;

import in.lekhai.voucher.entity.VoucherCounter;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.repository.VoucherCounterRepository;
import org.springframework.stereotype.Service;

@Service
public class VoucherCounterService {
    private final VoucherCounterRepository voucherCounterRepository;

    public VoucherCounterService(VoucherCounterRepository voucherCounterRepository) {
        this.voucherCounterRepository = voucherCounterRepository;
    }

    // This method call will lock this row
    public VoucherCounter fetchNextVoucherCounter(VoucherType voucherType) {
        return voucherCounterRepository.findForUpdate(voucherType)
                .orElseThrow(() -> new IllegalStateException(
                        "Voucher counter not initialized for " + voucherType));
    }

    public void increaseVoucherCounter(VoucherCounter counter) {
        counter.setNextNumber(counter.getNextNumber() + 1);
        voucherCounterRepository.save(counter);
    }
}
