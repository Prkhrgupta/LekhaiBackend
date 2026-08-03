package in.lekhai.voucher.service;

import in.lekhai.contract.model.ContraVoucherRequest;
import in.lekhai.contract.model.VoucherEntry;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
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
public class ContraVoucherService extends VoucherProcessor<ContraVoucherRequest> {
    private static final String BANK_ACCOUNTS_GROUP = "Bank Accounts";
    private static final String CASH_IN_HAND_GROUP = "Cash in Hand";

    private final LedgerRepository ledgerRepository;
    private final AccountGroupRepository accountGroupRepository;

    public ContraVoucherService(
            VoucherPostingService voucherPostingService,
            VoucherPostingMapper voucherPostingMapper,
            LedgerRepository ledgerRepository,
            AccountGroupRepository accountGroupRepository
    ) {
        super(voucherPostingService, voucherPostingMapper);
        this.ledgerRepository = ledgerRepository;
        this.accountGroupRepository = accountGroupRepository;
    }

    /**
     * 1. at least one debit entry and at least one credit entry
     * 2. every debit and credit account is in Bank or Cash In Hand Acc grp
     * 3. each entry amount is present and greater than zero
     * 4. total debit equals total credit
     */
    @Override
    void validate(ContraVoucherRequest voucher) {
    }

    @Override
    PostingRequest covertToVoucherPostRequest(ContraVoucherRequest contraVoucher) {
        List<PostingEntry> postingEntryList = new ArrayList<>();

        // Debit entries ( to / destination )
        for (VoucherEntry entry : contraVoucher.getDebitEntries()) {
            postingEntryList.add(new PostingEntry(
                    entry.getAccountId(),
                    entry.getAmount(),
                    BigDecimal.ZERO,
                    entry.getRemarks()
            ));
        }

        // Credit entries ( from / source )
        for (VoucherEntry entry : contraVoucher.getCreditEntries()) {
            postingEntryList.add(new PostingEntry(
                    entry.getAccountId(),
                    BigDecimal.ZERO,
                    entry.getAmount(),
                    entry.getRemarks()
            ));
        }

        return new PostingRequest(
                VoucherType.CONTRA,
                contraVoucher.getVoucherDate(),
                contraVoucher.getNarration(),
                postingEntryList
        );
    }
}
