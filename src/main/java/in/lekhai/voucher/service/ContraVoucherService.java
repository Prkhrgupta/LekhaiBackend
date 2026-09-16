package in.lekhai.voucher.service;

import in.lekhai.contract.model.ContraVoucherRequest;
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
public class ContraVoucherService extends VoucherProcessor<ContraVoucherRequest> {
    private final VoucherLedgerValidator ledgerValidator;

    public ContraVoucherService(
            VoucherPostingService voucherPostingService,
            VoucherPostingMapper voucherPostingMapper,
            VoucherLedgerValidator ledgerValidator
    ) {
        super(voucherPostingService, voucherPostingMapper);
        this.ledgerValidator = ledgerValidator;
    }

    /**
     * 1. at least one debit entry and at least one credit entry
     * 2. every debit and credit account is in Bank or Cash In Hand Acc grp
     * 3. each entry amount is present and greater than zero
     * 4. total debit equals total credit
     */
    @Override
    void validate(ContraVoucherRequest voucher) {
        if (voucher == null) {
            throw new LekhaiClientException("Contra voucher request must not be null", HttpStatus.BAD_REQUEST);
        }
        if (voucher.getVoucherDate() == null) {
            throw new LekhaiClientException("Voucher date must not be null", HttpStatus.BAD_REQUEST);
        }
        if (voucher.getDebitEntries() == null || voucher.getDebitEntries().isEmpty()) {
            throw new LekhaiClientException(
                    "Contra voucher must contain at least one debit entry",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (voucher.getCreditEntries() == null || voucher.getCreditEntries().isEmpty()) {
            throw new LekhaiClientException(
                    "Contra voucher must contain at least one credit entry",
                    HttpStatus.BAD_REQUEST
            );
        }
        BigDecimal totalDebit = sumContraEntries(voucher.getDebitEntries(), "debit");
        BigDecimal totalCredit = sumContraEntries(voucher.getCreditEntries(), "credit");
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new LekhaiClientException(
                    "Contra voucher total debit must equal total credit",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private BigDecimal sumContraEntries(List<VoucherEntry> entries, String side) {
        BigDecimal total = BigDecimal.ZERO;
        for (VoucherEntry entry : entries) {
            if (entry == null || entry.getAmount() == null || entry.getAmount().signum() <= 0) {
                throw new LekhaiClientException(
                        String.format("Each contra %s entry must have an amount greater than zero", side),
                        HttpStatus.BAD_REQUEST
                );
            }
            ledgerValidator.requireCashOrBankLedger(entry.getAccountId(), String.format("contra %s ledger", side));
            total = total.add(entry.getAmount());
        }
        return total;
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
