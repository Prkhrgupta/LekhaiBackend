package in.lekhai.voucher.service;

import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.contract.model.VoucherEntry;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.service.posting.VoucherPostingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReceiptVoucherService extends VoucherProcessor<ReceiptVoucherRequest> {
    private final VoucherLedgerValidator ledgerValidator;

    public ReceiptVoucherService(
            VoucherPostingService voucherPostingService,
            VoucherPostingMapper voucherPostingMapper,
            VoucherLedgerValidator ledgerValidator
    ) {
        super(voucherPostingService, voucherPostingMapper);
        this.ledgerValidator = ledgerValidator;
    }

    /**
     * 1. check the receiptAccountId (Dr. acc) is in Bank or Cash In Hand Acc grp
     * 2. accountId in receiptVoucherEntry should not be in Bank or Cash (This is the use of Contra)
     * 3. at least 1 receiptEntry
     */
    @Override
    void validate(ReceiptVoucherRequest voucher) {
        if (voucher == null) {
            throw new LekhaiClientException("Receipt voucher request must not be null", HttpStatus.BAD_REQUEST);
        }
        if (voucher.getVoucherDate() == null) {
            throw new LekhaiClientException("Voucher date must not be null", HttpStatus.BAD_REQUEST);
        }
        ledgerValidator.requireCashOrBankLedger(voucher.getReceiptAccountId(), "receipt ledger");
        if (voucher.getItems() == null || voucher.getItems().isEmpty()) {
            throw new LekhaiClientException(
                    "Receipt voucher must contain at least one entry",
                    HttpStatus.BAD_REQUEST
            );
        }
        for (VoucherEntry entry : voucher.getItems()) {
            requireReceiptEntry(entry);
        }
    }

    private void requireReceiptEntry(VoucherEntry entry) {
        if (entry == null || entry.getAmount() == null || entry.getAmount().signum() <= 0) {
            throw new LekhaiClientException(
                    "Each receipt entry must have an amount greater than zero",
                    HttpStatus.BAD_REQUEST
            );
        }
        ledgerValidator.requireNonCashOrBankLedger(entry.getAccountId(), "receipt entry ledger");
    }

    @Override
    PostingRequest covertToVoucherPostRequest(ReceiptVoucherRequest receiptVoucher) {
        List<PostingEntry> postingEntryList = new ArrayList<>();
        BigDecimal totalCreditAmount = BigDecimal.ZERO;

        // Credit entries
        for(VoucherEntry entry : receiptVoucher.getItems()) {
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
