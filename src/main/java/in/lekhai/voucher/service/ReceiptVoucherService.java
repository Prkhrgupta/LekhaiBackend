package in.lekhai.voucher.service;

import in.lekhai.contract.model.ReceiptVoucherEntry;
import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.service.posting.VoucherPostingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ReceiptVoucherService extends VoucherProcessor<ReceiptVoucherRequest> {
    private static final String BANK_ACCOUNTS_GROUP = "Bank Accounts";
    private static final String CASH_IN_HAND_GROUP = "Cash in Hand";

    private final LedgerRepository ledgerRepository;
    private final AccountGroupRepository accountGroupRepository;

    public ReceiptVoucherService(
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
     * 1. check the receiptAccountId (Dr. acc) is in Bank or Cash In Hand Acc grp
     * 2. accountId in receiptVoucherEntry should not be in Bank or Cash (This is the use of Contra)
     * 3. at least 1 receiptEntry
     */
    @Override
    void validate(ReceiptVoucherRequest voucher) {
    }

    @Override
    PostingRequest covertToVoucherPostRequest(ReceiptVoucherRequest receiptVoucher) {
        List<PostingEntry> postingEntryList = new ArrayList<>();
        BigDecimal totalCreditAmount = BigDecimal.ZERO;

        // Credit entries
        for(ReceiptVoucherEntry entry : receiptVoucher.getItems()) {
            PostingEntry postingEntry = new PostingEntry(
                    entry.getAccountId(),
                    BigDecimal.ZERO,
                    entry.getAmount(),
                    entry.getRemarks()
            );
            postingEntryList.add(postingEntry);
            totalCreditAmount = totalCreditAmount.add(entry.getAmount());
        }

        // Debit entry ( only one acc )
        postingEntryList.add(new PostingEntry(
                receiptVoucher.getReceiptAccountId(),
                totalCreditAmount,
                BigDecimal.ZERO,
                receiptVoucher.getNarration()
        ));

        // convert to actual posting request
        return new PostingRequest(
                VoucherType.RECEIPT,
                receiptVoucher.getVoucherDate(),
                receiptVoucher.getNarration(),
                postingEntryList
        );
    }
}
