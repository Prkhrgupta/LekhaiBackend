package in.lekhai.voucher.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import in.lekhai.contract.model.ContraVoucherRequest;
import in.lekhai.contract.model.JournalVoucherRequest;
import in.lekhai.contract.model.PaymentVoucherRequest;
import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.contract.model.VoucherEntry;
import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.service.posting.VoucherPostingService;

@Service
public class VoucherIntakeService {
    private final VoucherPostingService voucherPostingService;
    private final VoucherPostingMapper voucherPostingMapper;
    private final VoucherLedgerValidator ledgerValidator;

    public VoucherIntakeService(
            VoucherPostingService voucherPostingService,
            VoucherPostingMapper voucherPostingMapper,
            VoucherLedgerValidator ledgerValidator
    ) {
        this.voucherPostingService = voucherPostingService;
        this.voucherPostingMapper = voucherPostingMapper;
        this.ledgerValidator = ledgerValidator;
    }

    /**
     * Money paid out: item ledgers take the debit, the single cash-or-bank ledger takes the credit.
     */
    public VoucherResponse processPayment(PaymentVoucherRequest voucher) {
        if (voucher == null) {
            throw clientError("Payment voucher request must not be null");
        }
        requireVoucherDate(voucher.getVoucherDate());
        ledgerValidator.requireCashOrBankLedger(voucher.getPaymentAccountId(), "payment ledger");
        requireEntriesPresent(voucher.getItems(), "Payment voucher must contain at least one entry");
        sumEntries(voucher.getItems(), LedgerKind.NON_CASH_OR_BANK,
                "Each payment entry must have an amount greater than zero", "payment entry ledger");
        return post(convertDirectional(voucher.getItems(), voucher.getPaymentAccountId(),
                voucher.getVoucherDate(), voucher.getNarration(), true, VoucherType.PAYMENT));
    }

    /**
     * Money received: item ledgers take the credit, the single cash-or-bank ledger takes the debit.
     */
    public VoucherResponse processReceipt(ReceiptVoucherRequest voucher) {
        if (voucher == null) {
            throw clientError("Receipt voucher request must not be null");
        }
        requireVoucherDate(voucher.getVoucherDate());
        ledgerValidator.requireCashOrBankLedger(voucher.getReceiptAccountId(), "receipt ledger");
        requireEntriesPresent(voucher.getItems(), "Receipt voucher must contain at least one entry");
        sumEntries(voucher.getItems(), LedgerKind.NON_CASH_OR_BANK,
                "Each receipt entry must have an amount greater than zero", "receipt entry ledger");
        return post(convertDirectional(voucher.getItems(), voucher.getReceiptAccountId(),
                voucher.getVoucherDate(), voucher.getNarration(), false, VoucherType.RECEIPT));
    }

    /**
     * Cash moved between the firm's own cash and bank ledgers; no outside party involved.
     */
    public VoucherResponse processContra(ContraVoucherRequest voucher) {
        if (voucher == null) {
            throw clientError("Contra voucher request must not be null");
        }
        requireVoucherDate(voucher.getVoucherDate());
        requireEntriesPresent(voucher.getDebitEntries(), "Contra voucher must contain at least one debit entry");
        requireEntriesPresent(voucher.getCreditEntries(), "Contra voucher must contain at least one credit entry");
        BigDecimal totalDebit = sumEntries(voucher.getDebitEntries(), LedgerKind.CASH_OR_BANK,
                "Each contra debit entry must have an amount greater than zero", "contra debit ledger");
        BigDecimal totalCredit = sumEntries(voucher.getCreditEntries(), LedgerKind.CASH_OR_BANK,
                "Each contra credit entry must have an amount greater than zero", "contra credit ledger");
        requireBalanced(totalDebit, totalCredit, "Contra voucher total debit must equal total credit");
        return post(convertTwoSided(voucher.getDebitEntries(), voucher.getCreditEntries(),
                voucher.getVoucherDate(), voucher.getNarration(), VoucherType.CONTRA));
    }

    /**
     * Non-cash adjustments and corrections against any ledger, debits balancing credits.
     */
    public VoucherResponse processJournal(JournalVoucherRequest voucher) {
        if (voucher == null) {
            throw clientError("Journal voucher request must not be null");
        }
        requireVoucherDate(voucher.getVoucherDate());
        requireEntriesPresent(voucher.getDebitEntries(), "Journal voucher must contain at least one debit entry");
        requireEntriesPresent(voucher.getCreditEntries(), "Journal voucher must contain at least one credit entry");
        BigDecimal totalDebit = sumEntries(voucher.getDebitEntries(), LedgerKind.ANY,
                "Each journal debit entry must have an amount greater than zero", "journal debit ledger");
        BigDecimal totalCredit = sumEntries(voucher.getCreditEntries(), LedgerKind.ANY,
                "Each journal credit entry must have an amount greater than zero", "journal credit ledger");
        requireBalanced(totalDebit, totalCredit, "Journal voucher total debit must equal total credit");
        return post(convertTwoSided(voucher.getDebitEntries(), voucher.getCreditEntries(),
                voucher.getVoucherDate(), voucher.getNarration(), VoucherType.JOURNAL));
    }

    private void requireVoucherDate(LocalDate voucherDate) {
        if (voucherDate == null) {
            throw clientError("Voucher date must not be null");
        }
    }

    private void requireEntriesPresent(List<VoucherEntry> entries, String emptyMessage) {
        if (entries == null || entries.isEmpty()) {
            throw clientError(emptyMessage);
        }
    }

    private void requireBalanced(BigDecimal totalDebit, BigDecimal totalCredit, String message) {
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw clientError(message);
        }
    }

    private BigDecimal sumEntries(List<VoucherEntry> entries, LedgerKind kind, String amountMessage, String role) {
        BigDecimal total = BigDecimal.ZERO;
        for (VoucherEntry entry : entries) {
            if (entry == null || entry.getAmount() == null || entry.getAmount().signum() <= 0) {
                throw clientError(amountMessage);
            }
            requireLedgerKind(entry.getAccountId(), kind, role);
            total = total.add(entry.getAmount());
        }
        return total;
    }

    private void requireLedgerKind(Long ledgerId, LedgerKind kind, String role) {
        switch (kind) {
            case CASH_OR_BANK -> ledgerValidator.requireCashOrBankLedger(ledgerId, role);
            case NON_CASH_OR_BANK -> ledgerValidator.requireNonCashOrBankLedger(ledgerId, role);
            case ANY -> ledgerValidator.requireLedger(ledgerId);
        }
    }

    private PostingRequest convertDirectional(List<VoucherEntry> items, Long cashAccountId, LocalDate voucherDate,
            String narration, boolean cashIsCredit, VoucherType voucherType) {
        List<PostingEntry> postings = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (VoucherEntry item : items) {
            BigDecimal debit = cashIsCredit ? item.getAmount() : BigDecimal.ZERO;
            BigDecimal credit = cashIsCredit ? BigDecimal.ZERO : item.getAmount();
            postings.add(new PostingEntry(item.getAccountId(), debit, credit, item.getRemarks()));
            total = total.add(item.getAmount());
        }
        BigDecimal cashDebit = cashIsCredit ? BigDecimal.ZERO : total;
        BigDecimal cashCredit = cashIsCredit ? total : BigDecimal.ZERO;
        postings.add(new PostingEntry(cashAccountId, cashDebit, cashCredit, narration));
        return new PostingRequest(voucherType, voucherDate, narration, postings);
    }

    private PostingRequest convertTwoSided(List<VoucherEntry> debitEntries, List<VoucherEntry> creditEntries,
            LocalDate voucherDate, String narration, VoucherType voucherType) {
        List<PostingEntry> postings = new ArrayList<>();
        for (VoucherEntry entry : debitEntries) {
            postings.add(new PostingEntry(entry.getAccountId(), entry.getAmount(), BigDecimal.ZERO,
                    entry.getRemarks()));
        }
        for (VoucherEntry entry : creditEntries) {
            postings.add(new PostingEntry(entry.getAccountId(), BigDecimal.ZERO, entry.getAmount(),
                    entry.getRemarks()));
        }
        return new PostingRequest(voucherType, voucherDate, narration, postings);
    }

    private VoucherResponse post(PostingRequest posting) {
        Voucher createdVoucher = voucherPostingService.post(posting);
        return voucherPostingMapper.mapToCommonVoucherResponse(createdVoucher);
    }

    private LekhaiClientException clientError(String message) {
        return new LekhaiClientException(message, HttpStatus.BAD_REQUEST);
    }

    private enum LedgerKind {
        CASH_OR_BANK,
        NON_CASH_OR_BANK,
        ANY
    }
}
