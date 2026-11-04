package in.lekhai.salesinvoice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Quantitative stock movement; never negative. Quantities are implicitly in the item's primary UOM.
 */
@Table("stock_ledger")
public record StockLedger(
        @Id Long id,
        Long stockItemId,
        LocalDate transactionDate,
        Long voucherId,
        String transactionType,
        BigDecimal inQty,
        BigDecimal inRate,
        BigDecimal inValue,
        BigDecimal outQty,
        BigDecimal outRate,
        BigDecimal outValue
) {
    public StockLedger withId(Long newId) {
        return new StockLedger(newId, stockItemId, transactionDate, voucherId, transactionType,
                inQty, inRate, inValue, outQty, outRate, outValue);
    }
}
