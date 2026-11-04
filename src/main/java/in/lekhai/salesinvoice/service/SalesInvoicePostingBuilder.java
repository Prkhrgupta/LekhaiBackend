package in.lekhai.salesinvoice.service;

import in.lekhai.salesinvoice.calc.InvoiceModels.TaxRow;
import in.lekhai.voucher.dto.posting.PostingEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the double-entry lines: Dr party for net; Cr sale ledgers (grouped), Cr tax ledgers; round-off on the
 * balancing side. Zero lines are skipped.
 */
@Component
class SalesInvoicePostingBuilder {

    List<PostingEntry> build(PreparedInvoice invoice) {
        BigDecimal net = invoice.calc().totals().netAmount();
        List<PostingEntry> entries = new ArrayList<>();
        entries.add(new PostingEntry(invoice.party().ledgerId(), net, BigDecimal.ZERO, "Sales invoice"));
        saleCredits(invoice).forEach((ledgerId, amount) -> addCredit(entries, ledgerId, amount, "Sales"));
        List<TaxRow> taxes = invoice.calc().taxes();
        for (int i = 0; i < taxes.size(); i++) {
            addCredit(entries, invoice.taxLedgerIds().get(i), taxes.get(i).amount(), taxes.get(i).taxType());
        }
        addRoundOff(entries, invoice);
        return entries;
    }

    /**
     * Header-ledger validation: the party debit must equal the invoice net amount.
     */
    void assertPartyDebitMatchesNet(List<PostingEntry> entries, BigDecimal netAmount) {
        BigDecimal partyDebit = entries.get(0).debitAmount();
        if (partyDebit.compareTo(netAmount) != 0) {
            throw new IllegalStateException(
                    String.format("Party debit %s does not match invoice net %s", partyDebit, netAmount));
        }
    }

    private Map<Long, BigDecimal> saleCredits(PreparedInvoice invoice) {
        Map<Long, BigDecimal> byLedger = new LinkedHashMap<>();
        for (int i = 0; i < invoice.saleLedgerIds().size(); i++) {
            BigDecimal taxable = invoice.calc().lines().get(i).taxableAmount();
            byLedger.merge(invoice.saleLedgerIds().get(i), taxable, BigDecimal::add);
        }
        return byLedger;
    }

    private void addRoundOff(List<PostingEntry> entries, PreparedInvoice invoice) {
        BigDecimal roundOff = invoice.calc().totals().roundOff();
        if (roundOff.signum() > 0) {
            addCredit(entries, invoice.roundOffLedgerId(), roundOff, "Round off");
        } else if (roundOff.signum() < 0) {
            entries.add(new PostingEntry(invoice.roundOffLedgerId(), roundOff.negate(), BigDecimal.ZERO, "Round off"));
        }
    }

    private void addCredit(List<PostingEntry> entries, Long ledgerId, BigDecimal amount, String remarks) {
        if (amount.signum() > 0) {
            entries.add(new PostingEntry(ledgerId, BigDecimal.ZERO, amount, remarks));
        }
    }
}
