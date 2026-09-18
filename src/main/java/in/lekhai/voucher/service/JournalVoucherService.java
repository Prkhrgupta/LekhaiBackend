package in.lekhai.voucher.service;

import in.lekhai.contract.model.JournalVoucherRequest;
import in.lekhai.contract.model.VoucherEntry;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.service.posting.VoucherPostingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class JournalVoucherService extends VoucherProcessor<JournalVoucherRequest> {
    private final LedgerRepository ledgerRepository;

    public JournalVoucherService(
            VoucherPostingService voucherPostingService,
            VoucherPostingMapper voucherPostingMapper,
            LedgerRepository ledgerRepository
    ) {
        super(voucherPostingService, voucherPostingMapper);
        this.ledgerRepository = ledgerRepository;
    }

    /**
     * 1. at least one debit entry and at least one credit entry
     * 2. each entry amount is present and greater than zero
     * 3. total debit equals total credit
     * 4. every accountId exists
     */
    @Override
    void validate(JournalVoucherRequest voucher) {
    }

    @Override
    PostingRequest covertToVoucherPostRequest(JournalVoucherRequest journalVoucher) {
        List<PostingEntry> postingEntryList = new ArrayList<>();

        for (VoucherEntry entry : journalVoucher.getDebitEntries()) {
            postingEntryList.add(new PostingEntry(
                    entry.getAccountId(),
                    entry.getAmount(),
                    BigDecimal.ZERO,
                    entry.getRemarks()
            ));
        }

        for (VoucherEntry entry : journalVoucher.getCreditEntries()) {
            postingEntryList.add(new PostingEntry(
                    entry.getAccountId(),
                    BigDecimal.ZERO,
                    entry.getAmount(),
                    entry.getRemarks()
            ));
        }

        return new PostingRequest(
                VoucherType.JOURNAL,
                journalVoucher.getVoucherDate(),
                journalVoucher.getNarration(),
                postingEntryList
        );
    }
}
