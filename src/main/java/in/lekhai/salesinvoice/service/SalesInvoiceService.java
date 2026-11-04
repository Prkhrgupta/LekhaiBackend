package in.lekhai.salesinvoice.service;

import in.lekhai.contract.model.SalesInvoicePreviewResponse;
import in.lekhai.contract.model.SalesInvoiceRequest;
import in.lekhai.contract.model.SalesInvoiceResponse;
import in.lekhai.contract.model.SalesInvoiceWarning;
import in.lekhai.contract.model.SalesInvoiceWarningCode;
import in.lekhai.contract.model.VoucherType;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;
import in.lekhai.salesinvoice.calc.InvoiceModels.Totals;
import in.lekhai.salesinvoice.entity.SalesInvoice;
import in.lekhai.salesinvoice.mapper.SalesInvoiceMapper;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.dto.posting.PostingResult;
import in.lekhai.voucher.entity.CustomVoucherSeries;
import in.lekhai.voucher.repository.CustomVoucherSeriesRepository;
import in.lekhai.voucher.service.posting.VoucherPostingService;
import in.lekhai.voucher.util.FinancialYear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates sales invoice creation: prepare, warn on stock, post the SALES voucher, persist the invoice
 * snapshot and write stock-out rows, all in one shop-scoped transaction.
 */
@Service
public class SalesInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(SalesInvoiceService.class);

    private final SalesInvoicePreparationService preparationService;
    private final SalesInvoicePostingBuilder postingBuilder;
    private final VoucherPostingService voucherPostingService;
    private final SalesInvoicePersistenceService persistenceService;
    private final StockLedgerService stockLedgerService;
    private final CustomVoucherSeriesRepository seriesRepository;
    private final SalesInvoiceMapper mapper;

    public SalesInvoiceService(SalesInvoicePreparationService preparationService,
                               SalesInvoicePostingBuilder postingBuilder,
                               VoucherPostingService voucherPostingService,
                               SalesInvoicePersistenceService persistenceService,
                               StockLedgerService stockLedgerService,
                               CustomVoucherSeriesRepository seriesRepository, SalesInvoiceMapper mapper) {
        this.preparationService = preparationService;
        this.postingBuilder = postingBuilder;
        this.voucherPostingService = voucherPostingService;
        this.persistenceService = persistenceService;
        this.stockLedgerService = stockLedgerService;
        this.seriesRepository = seriesRepository;
        this.mapper = mapper;
    }

    @ShopContextTransactional
    public SalesInvoiceResponse create(SalesInvoiceRequest request) {
        PreparedInvoice invoice = preparationService.prepare(request);
        List<SalesInvoiceWarning> warnings = stockWarnings(invoice);
        PostingResult posted = voucherPostingService.post(toPostingRequest(invoice));
        SalesInvoice saved = persistenceService.persist(invoice, posted);
        stockLedgerService.recordSale(invoice, posted.voucher().getId());
        addGapWarning(warnings, posted.firstMissingBillNo());
        return mapper.toResponse(invoice, saved, warnings);
    }

    @ShopContextTransactional
    public SalesInvoicePreviewResponse preview(SalesInvoiceRequest request) {
        PreparedInvoice invoice = preparationService.prepare(request);
        String fy = FinancialYear.from(invoice.billDate());
        CustomVoucherSeries series = seriesRepository
                .findByVoucherTypeAndSeriesCodeAndFinancialYear(VoucherType.SALES, invoice.seriesCode(), fy)
                .orElseThrow(() -> new SeriesFyMissingException(VoucherType.SALES.name(), fy));
        Long billNo = invoice.manualBillNo() != null ? invoice.manualBillNo() : series.getNextNumber();
        String display = nullToEmpty(series.getPrefix()) + billNo + nullToEmpty(series.getSuffix());
        return mapper.toPreview(invoice, billNo, display, fy, stockWarnings(invoice));
    }

    private PostingRequest toPostingRequest(PreparedInvoice invoice) {
        List<PostingEntry> entries = postingBuilder.build(invoice);
        Totals totals = invoice.calc().totals();
        postingBuilder.assertPartyDebitMatchesNet(entries, totals.netAmount());
        String narration = invoice.remarks() == null ? "Sales invoice" : invoice.remarks();
        return new PostingRequest(VoucherType.SALES, invoice.billDate(), narration, entries,
                invoice.seriesCode(), invoice.manualBillNo());
    }

    private List<SalesInvoiceWarning> stockWarnings(PreparedInvoice invoice) {
        List<SalesInvoiceWarning> warnings = new ArrayList<>();
        requestedByItem(invoice).forEach((itemId, requested) -> {
            BigDecimal available = stockLedgerService.availableQty(itemId);
            if (requested.compareTo(available) > 0) {
                warnings.add(negativeStockWarning(invoice, itemId, available, requested));
            }
        });
        return warnings;
    }

    private Map<Long, BigDecimal> requestedByItem(PreparedInvoice invoice) {
        Map<Long, BigDecimal> requested = new LinkedHashMap<>();
        for (int i = 0; i < invoice.snapshots().size(); i++) {
            requested.merge(invoice.snapshots().get(i).stockItemId(), invoice.inputs().get(i).quantity(),
                    BigDecimal::add);
        }
        return requested;
    }

    private SalesInvoiceWarning negativeStockWarning(PreparedInvoice invoice, Long itemId,
                                                     BigDecimal available, BigDecimal requested) {
        String name = invoice.snapshots().stream().filter(s -> s.stockItemId().equals(itemId))
                .findFirst().map(s -> s.itemName()).orElse("Item " + itemId);
        String message = String.format("%s: available %s, requested %s", name,
                available.stripTrailingZeros().toPlainString(), requested.stripTrailingZeros().toPlainString());
        log.warn("Negative stock on sales invoice: {}", message);
        return new SalesInvoiceWarning().code(SalesInvoiceWarningCode.NEGATIVE_STOCK).message(message)
                .stockItemId(itemId);
    }

    private void addGapWarning(List<SalesInvoiceWarning> warnings, Long firstMissingBillNo) {
        if (firstMissingBillNo != null) {
            warnings.add(new SalesInvoiceWarning().code(SalesInvoiceWarningCode.SERIES_GAP)
                    .message("Bill no " + firstMissingBillNo + " is missing"));
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

}
