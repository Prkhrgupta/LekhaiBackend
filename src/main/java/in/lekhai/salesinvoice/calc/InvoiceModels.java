package in.lekhai.salesinvoice.calc;

import java.math.BigDecimal;
import java.util.List;

/**
 * Value objects shared by {@link InvoiceCalculator} and the invoice service.
 */
public final class InvoiceModels {

    private InvoiceModels() {
    }

    public record LineInput(
            BigDecimal quantity,
            BigDecimal rate,
            BigDecimal discountPct,
            BigDecimal gstRate,
            BigDecimal cessRate) {
    }

    public record LineResult(
            BigDecimal grossAmount,
            BigDecimal discountAmount,
            BigDecimal taxableAmount,
            BigDecimal cgstAmount,
            BigDecimal sgstAmount,
            BigDecimal igstAmount,
            BigDecimal cessAmount,
            BigDecimal lineTotal) {
    }

    /**
     * Rolled-up tax row. {@code slabRate} is the full GST slab (or cess rate) used to look up the tax ledger.
     */
    public record TaxRow(
            String taxType,
            BigDecimal rate,
            BigDecimal slabRate,
            BigDecimal taxableAmount,
            BigDecimal amount) {
    }

    public record Totals(
            BigDecimal goodsAmount,
            BigDecimal discountAmount,
            BigDecimal taxableAmount,
            BigDecimal cgstAmount,
            BigDecimal sgstAmount,
            BigDecimal igstAmount,
            BigDecimal cessAmount,
            BigDecimal roundOff,
            BigDecimal netAmount) {
    }

    public record CalcResult(List<LineResult> lines, List<TaxRow> taxes, Totals totals) {
    }
}
