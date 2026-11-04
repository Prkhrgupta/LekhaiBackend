package in.lekhai.salesinvoice.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("sales_invoice_item")
public record SalesInvoiceItem(
        @Id Long id,
        Long salesInvoiceId,
        Integer srno,
        Long stockItemId,
        String itemNameSnapshot,
        String hsnCodeSnapshot,
        String categoryNameSnapshot,
        Long saleLedgerId,
        BigDecimal quantity,
        Long uomId,
        String uomSnapshot,
        BigDecimal alternateQuantity,
        BigDecimal rate,
        BigDecimal grossAmount,
        BigDecimal discountPct,
        BigDecimal discountAmount,
        BigDecimal taxableValue,
        BigDecimal gstRateSnapshot,
        BigDecimal cessRateSnapshot,
        BigDecimal cessAmount
) {
    public SalesInvoiceItem withId(Long newId) {
        return new SalesInvoiceItem(newId, salesInvoiceId, srno, stockItemId, itemNameSnapshot,
                hsnCodeSnapshot, categoryNameSnapshot, saleLedgerId, quantity, uomId, uomSnapshot,
                alternateQuantity, rate, grossAmount, discountPct, discountAmount, taxableValue,
                gstRateSnapshot, cessRateSnapshot, cessAmount);
    }
}
