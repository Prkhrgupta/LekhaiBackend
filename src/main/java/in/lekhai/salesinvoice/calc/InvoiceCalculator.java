package in.lekhai.salesinvoice.calc;

import in.lekhai.salesinvoice.calc.InvoiceModels.CalcResult;
import in.lekhai.salesinvoice.calc.InvoiceModels.LineInput;
import in.lekhai.salesinvoice.calc.InvoiceModels.LineResult;
import in.lekhai.salesinvoice.calc.InvoiceModels.TaxRow;
import in.lekhai.salesinvoice.calc.InvoiceModels.Totals;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pure GST maths. Taxes are summed unrounded per rate slab and rounded to 2dp only once, so paise do not drift.
 */
@Component
public class InvoiceCalculator {

    public static final String CGST = "CGST";
    public static final String SGST = "SGST";
    public static final String IGST = "IGST";
    public static final String CESS = "CESS";

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal TWO = BigDecimal.valueOf(2);
    private static final MathContext PRECISE = new MathContext(20, RoundingMode.HALF_UP);

    public CalcResult calculate(List<LineInput> inputs, boolean interState) {
        Map<BigDecimal, BigDecimal[]> gstBySlab = new TreeMap<>();
        Map<BigDecimal, BigDecimal[]> cessByRate = new TreeMap<>();
        List<LineResult> lines = new ArrayList<>();
        for (LineInput input : inputs) {
            LineResult line = calculateLine(input, interState);
            lines.add(line);
            accumulate(gstBySlab, input.gstRate(), line.taxableAmount());
            accumulate(cessByRate, input.cessRate(), line.taxableAmount());
        }
        List<TaxRow> taxes = buildTaxRows(gstBySlab, cessByRate, interState);
        return new CalcResult(lines, taxes, buildTotals(lines, taxes));
    }

    private LineResult calculateLine(LineInput input, boolean interState) {
        BigDecimal gross = money(input.quantity().multiply(input.rate()));
        BigDecimal discount = money(gross.multiply(input.discountPct()).divide(HUNDRED, PRECISE));
        BigDecimal taxable = gross.subtract(discount);
        BigDecimal gst = taxable.multiply(input.gstRate()).divide(HUNDRED, PRECISE);
        BigDecimal cess = money(taxable.multiply(input.cessRate()).divide(HUNDRED, PRECISE));
        BigDecimal half = money(gst.divide(TWO, PRECISE));
        BigDecimal igst = interState ? money(gst) : BigDecimal.ZERO.setScale(2);
        BigDecimal cgst = interState ? BigDecimal.ZERO.setScale(2) : half;
        BigDecimal total = taxable.add(cgst).add(cgst).add(igst).add(cess);
        return new LineResult(gross, discount, taxable, cgst, cgst, igst, cess, total);
    }

    private void accumulate(Map<BigDecimal, BigDecimal[]> bucket, BigDecimal rate, BigDecimal taxable) {
        if (rate.signum() == 0) {
            return;
        }
        BigDecimal[] sums = bucket.computeIfAbsent(rate.stripTrailingZeros(),
                key -> new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO});
        sums[0] = sums[0].add(taxable);
        sums[1] = sums[1].add(taxable.multiply(rate).divide(HUNDRED, PRECISE));
    }

    private List<TaxRow> buildTaxRows(Map<BigDecimal, BigDecimal[]> gstBySlab,
                                      Map<BigDecimal, BigDecimal[]> cessByRate, boolean interState) {
        List<TaxRow> rows = new ArrayList<>();
        gstBySlab.forEach((slab, sums) -> {
            if (interState) {
                addRow(rows, IGST, slab, slab, sums[0], sums[1]);
                return;
            }
            BigDecimal half = slab.divide(TWO, PRECISE).stripTrailingZeros();
            addRow(rows, CGST, half, slab, sums[0], sums[1].divide(TWO, PRECISE));
            addRow(rows, SGST, half, slab, sums[0], sums[1].divide(TWO, PRECISE));
        });
        cessByRate.forEach((rate, sums) -> addRow(rows, CESS, rate, rate, sums[0], sums[1]));
        return rows;
    }

    private void addRow(List<TaxRow> rows, String type, BigDecimal rate, BigDecimal slab,
                        BigDecimal taxable, BigDecimal unroundedAmount) {
        BigDecimal amount = money(unroundedAmount);
        if (amount.signum() == 0) {
            return;
        }
        rows.add(new TaxRow(type, rate, slab, money(taxable), amount));
    }

    private Totals buildTotals(List<LineResult> lines, List<TaxRow> taxes) {
        BigDecimal goods = sum(lines.stream().map(LineResult::grossAmount).toList());
        BigDecimal discount = sum(lines.stream().map(LineResult::discountAmount).toList());
        BigDecimal taxable = sum(lines.stream().map(LineResult::taxableAmount).toList());
        BigDecimal cgst = taxOf(taxes, CGST);
        BigDecimal sgst = taxOf(taxes, SGST);
        BigDecimal igst = taxOf(taxes, IGST);
        BigDecimal cess = taxOf(taxes, CESS);
        BigDecimal exact = taxable.add(cgst).add(sgst).add(igst).add(cess);
        BigDecimal net = exact.setScale(0, RoundingMode.HALF_UP).setScale(2);
        return new Totals(goods, discount, taxable, cgst, sgst, igst, cess, net.subtract(exact), net);
    }

    private BigDecimal taxOf(List<TaxRow> taxes, String type) {
        return sum(taxes.stream().filter(row -> row.taxType().equals(type)).map(TaxRow::amount).toList());
    }

    private BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
