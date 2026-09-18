package in.lekhai.voucher.service.posting;

import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherCounter;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import in.lekhai.voucher.service.VoucherCounterService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VoucherPostingService {
    private final VoucherCounterService voucherCounterService;
    private final VoucherPostingMapper voucherPostingMapper;
    private final VoucherRepository voucherRepository;
    private final VoucherEntryRepository voucherEntryRepository;

    public VoucherPostingService(
            VoucherCounterService voucherCounterService,
            VoucherPostingMapper voucherPostingMapper,
            VoucherRepository voucherRepository,
            VoucherEntryRepository voucherEntryRepository
    ) {
        this.voucherCounterService = voucherCounterService;
        this.voucherPostingMapper = voucherPostingMapper;
        this.voucherRepository = voucherRepository;
        this.voucherEntryRepository = voucherEntryRepository;
    }

    /**
     * generate a voucher number
     *  enter voucher and voucher entry
     */
    @ShopContextTransactional
    public Voucher post(PostingRequest request) {
        validate(request);
        VoucherCounter voucherCounter = voucherCounterService.fetchNextVoucherCounter(request.voucherType());
        Long voucherNo = voucherCounter.getNextNumber();
        Voucher voucher = voucherPostingMapper.mapToVoucher(request, voucherNo);
        Voucher savedVoucher = voucherRepository.save(voucher);

        int lineNumber = 1;
        List<VoucherEntry> entries = new ArrayList<>();
        for (PostingEntry entry : request.entries()) {
            entries.add(voucherPostingMapper.mapToVoucherEntry(
                    entry,
                    savedVoucher.getId(),
                    lineNumber++
            ));
        }
        voucherEntryRepository.saveAll(entries);

        // increase the voucherCounter
        voucherCounterService.increaseVoucherCounter(voucherCounter);

        return savedVoucher;
    }

    /**
     * 1. entries is not empty
     * 2. Each entry have one positive side and one side == 0 | Dr > 0 || Cr > 0
     * 3. Total Cr == Total Dr
     * 4. the ledgerId exists
     * 5. VoucherDate is Valid, i.e. it's in the financial year user is working on (will have this in JWT )
     */
    private void validate(PostingRequest request) {

    }
}
