package in.lekhai.voucher.service;

import in.lekhai.contract.model.JournalVoucherRequest;
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
public class JournalVoucherService extends VoucherProcessor<JournalVoucherRequest> {
    private final VoucherLedgerValidator ledgerValidator;

    public JournalVoucherService(
            VoucherPostingService voucherPostingService,
            VoucherPostingMapper voucherPostingMapper,
            VoucherLedgerValidator ledgerValidator
    ) {
        super(voucherPostingService, voucherPostingMapper);
        this.ledgerValidator = ledgerValidator;
    }

    /**
     * 1. at least one debit entry and at least one credit entry
     * 2. each entry amount is present and greater than zero
     * 3. total debit equals total credit
     * 4. every accountId exists
     */
    @Override
    void validate(JournalVoucherRequest voucher) {
        if (voucher == null) {
            throw new LekhaiClientException("Journal voucher request must not be null", HttpStatus.BAD_REQUEST);
        }
        if (voucher.getVoucherDate() == null) {
            throw new LekhaiClientException("Voucher date must not be null", HttpStatus.BAD_REQUEST);
        }
        if (voucher.getDebitEntries() == null || voucher.getDebitEntries().isEmpty()) {
            throw new LekhaiClientException(
                    "Journal voucher must contain at least one debit entry",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (voucher.getCreditEntries() == null || voucher.getCreditEntries().isEmpty()) {
            throw new LekhaiClientException(
                    "Journal voucher must contain at least one credit entry",
                    HttpStatus.BAD_REQUEST
            );
        }
        BigDecimal totalDebit = sumJournalEntries(voucher.getDebitEntries(), "debit");
        BigDecimal totalCredit = sumJournalEntries(voucher.getCreditEntries(), "credit");
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new LekhaiClientException(
                    "Journal voucher total debit must equal total credit",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private BigDecimal sumJournalEntries(List<VoucherEntry> entries, String side) {
        BigDecimal total = BigDecimal.ZERO;
        for (VoucherEntry entry : entries) {
            if (entry == null || entry.getAmount() == null || entry.getAmount().signum() <= 0) {
                throw new LekhaiClientException(
                        String.format("Each journal %s entry must have an amount greater than zero", side),
                        HttpStatus.BAD_REQUEST
                );
            }
            ledgerValidator.requireLedger(entry.getAccountId());
            total = total.add(entry.getAmount());
        }
        return total;
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
