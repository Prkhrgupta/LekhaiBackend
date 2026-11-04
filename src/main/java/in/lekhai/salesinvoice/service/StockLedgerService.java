package in.lekhai.salesinvoice.service;

import in.lekhai.contract.model.StockAvailabilityResponse;
import in.lekhai.core.inventory_master.domain.StockItem;
import in.lekhai.core.inventory_master.domain.Uom;
import in.lekhai.core.inventory_master.repository.StockItemRepository;
import in.lekhai.core.inventory_master.repository.UomRepository;
import in.lekhai.salesinvoice.entity.StockLedger;
import in.lekhai.salesinvoice.repository.StockLedgerRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Quantitative stock movements. Availability = opening_qty + SUM(in) - SUM(out). Negative stock is never blocked.
 */
@Service
public class StockLedgerService {

    static final String SALE = "SALE";

    private static final Logger log = LoggerFactory.getLogger(StockLedgerService.class);

    private final StockLedgerRepository stockLedgerRepository;
    private final StockItemRepository stockItemRepository;
    private final UomRepository uomRepository;

    public StockLedgerService(StockLedgerRepository stockLedgerRepository,
                              StockItemRepository stockItemRepository, UomRepository uomRepository) {
        this.stockLedgerRepository = stockLedgerRepository;
        this.stockItemRepository = stockItemRepository;
        this.uomRepository = uomRepository;
    }

    @ShopContextTransactional
    public StockAvailabilityResponse getAvailability(Long stockItemId) {
        StockItem item = stockItemRepository.findById(stockItemId)
                .filter(found -> !Boolean.TRUE.equals(found.getDeleted()))
                .orElseThrow(() -> SalesInvoiceErrors.notFound("Stock item [" + stockItemId + "] not found"));
        BigDecimal opening = item.getOpeningQty() == null ? BigDecimal.ZERO : item.getOpeningQty();
        BigDecimal in = stockLedgerRepository.sumInQty(stockItemId);
        BigDecimal out = stockLedgerRepository.sumOutQty(stockItemId);
        Uom uom = uomRepository.findById(item.getPrimaryUomId()).orElse(null);
        return new StockAvailabilityResponse()
                .stockItemId(stockItemId).itemName(item.getItemName())
                .uomId(item.getPrimaryUomId()).uomCode(uom == null ? null : uom.getQuantityCode())
                .openingQty(opening.doubleValue()).totalIn(in.doubleValue()).totalOut(out.doubleValue())
                .availableQty(opening.add(in).subtract(out).doubleValue());
    }

    /**
     * Available quantity for a stock item right now; used for negative-stock warnings.
     */
    public BigDecimal availableQty(Long stockItemId) {
        return BigDecimal.valueOf(getAvailability(stockItemId).getAvailableQty());
    }

    public void recordSale(PreparedInvoice invoice, Long voucherId) {
        List<StockLedger> rows = new java.util.ArrayList<>();
        for (int i = 0; i < invoice.snapshots().size(); i++) {
            rows.add(saleRow(invoice, i, voucherId));
        }
        stockLedgerRepository.saveAll(rows);
    }

    private StockLedger saleRow(PreparedInvoice invoice, int index, Long voucherId) {
        var snapshot = invoice.snapshots().get(index);
        BigDecimal qty = invoice.inputs().get(index).quantity();
        BigDecimal value = qty.multiply(snapshot.costRate()).setScale(2, RoundingMode.HALF_UP);
        if (snapshot.costRate().signum() == 0) {
            log.warn("Stock item {} has no purchase price or opening rate; out_value recorded as 0",
                    snapshot.stockItemId());
        }
        LocalDate date = invoice.billDate();
        return new StockLedger(null, snapshot.stockItemId(), date, voucherId, SALE,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, qty, snapshot.costRate(), value);
    }
}
