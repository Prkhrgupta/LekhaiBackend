package in.lekhai.salesinvoice.service;

import in.lekhai.salesinvoice.calc.InvoiceModels.LineInput;
import in.lekhai.salesinvoice.calc.InvoiceModels.LineResult;
import in.lekhai.salesinvoice.calc.InvoiceModels.TaxRow;
import in.lekhai.salesinvoice.calc.InvoiceModels.Totals;
import in.lekhai.salesinvoice.entity.SalesInvoice;
import in.lekhai.salesinvoice.entity.SalesInvoiceItem;
import in.lekhai.salesinvoice.entity.SalesInvoiceTax;
import in.lekhai.salesinvoice.repository.SalesInvoiceItemRepository;
import in.lekhai.salesinvoice.repository.SalesInvoiceRepository;
import in.lekhai.salesinvoice.repository.SalesInvoiceTaxRepository;
import in.lekhai.voucher.dto.posting.PostingResult;
import in.lekhai.voucher.util.FinancialYear;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Writes the invoice header, item and tax snapshot rows. Called inside the create transaction.
 */
@Service
class SalesInvoicePersistenceService {

    private static final String ACTIVE = "ACTIVE";

    private final SalesInvoiceRepository invoiceRepository;
    private final SalesInvoiceItemRepository itemRepository;
    private final SalesInvoiceTaxRepository taxRepository;

    SalesInvoicePersistenceService(SalesInvoiceRepository invoiceRepository,
                                   SalesInvoiceItemRepository itemRepository,
                                   SalesInvoiceTaxRepository taxRepository) {
        this.invoiceRepository = invoiceRepository;
        this.itemRepository = itemRepository;
        this.taxRepository = taxRepository;
    }

    SalesInvoice persist(PreparedInvoice invoice, PostingResult posted) {
        SalesInvoice saved = invoiceRepository.save(header(invoice, posted));
        itemRepository.saveAll(items(invoice, saved.id()));
        taxRepository.saveAll(taxes(invoice, saved.id()));
        return saved;
    }

    private SalesInvoice header(PreparedInvoice invoice, PostingResult posted) {
        Totals t = invoice.calc().totals();
        var voucher = posted.voucher();
        var party = invoice.party();
        return new SalesInvoice(null, voucher.getId(), voucher.getSeriesId(), invoice.seriesCode(),
                voucher.getVoucherNumber(), posted.displayVoucherNumber(),
                FinancialYear.from(invoice.billDate()), invoice.billDate(), party.ledgerId(), party.name(),
                party.gstin(), party.stateGstCode(), party.address(), party.address(), null,
                invoice.placeOfSupply(), invoice.placeOfSupplySource(), invoice.interState(),
                invoice.supplyType(), false, ACTIVE, t.goodsAmount(), t.discountAmount(),
                BigDecimal.ZERO.setScale(2), t.taxableAmount(), t.cgstAmount(), t.sgstAmount(), t.igstAmount(),
                t.cessAmount(), t.roundOff(), t.netAmount(), invoice.remarks(), null, null, Instant.now());
    }

    private List<SalesInvoiceItem> items(PreparedInvoice invoice, Long invoiceId) {
        return IntStream.range(0, invoice.snapshots().size()).mapToObj(i -> {
            var snap = invoice.snapshots().get(i);
            LineInput input = invoice.inputs().get(i);
            LineResult line = invoice.calc().lines().get(i);
            return new SalesInvoiceItem(null, invoiceId, i + 1, snap.stockItemId(), snap.itemName(),
                    snap.hsnCode(), snap.categoryName(), invoice.saleLedgerIds().get(i), input.quantity(),
                    snap.uomId(), snap.uomCode(), BigDecimal.ZERO, input.rate(), line.grossAmount(),
                    input.discountPct(), line.discountAmount(), line.taxableAmount(), snap.gstRate(),
                    snap.cessRate(), line.cessAmount());
        }).toList();
    }

    private List<SalesInvoiceTax> taxes(PreparedInvoice invoice, Long invoiceId) {
        return IntStream.range(0, invoice.calc().taxes().size()).mapToObj(i -> {
            TaxRow row = invoice.calc().taxes().get(i);
            return new SalesInvoiceTax(null, invoiceId, row.taxType(), row.rate(), row.taxableAmount(),
                    row.amount(), invoice.taxLedgerIds().get(i));
        }).toList();
    }
}
