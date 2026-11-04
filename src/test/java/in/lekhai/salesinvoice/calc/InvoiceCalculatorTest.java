package in.lekhai.salesinvoice.calc;

import in.lekhai.salesinvoice.calc.InvoiceModels.CalcResult;
import in.lekhai.salesinvoice.calc.InvoiceModels.LineInput;
import in.lekhai.salesinvoice.calc.InvoiceModels.TaxRow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceCalculatorTest {

    private final InvoiceCalculator calculator = new InvoiceCalculator();

    private LineInput line(String qty, String rate, String disc, String gst, String cess) {
        return new LineInput(new BigDecimal(qty), new BigDecimal(rate), new BigDecimal(disc),
                new BigDecimal(gst), new BigDecimal(cess));
    }

    @Test
    void intraStateSplitsGstIntoCgstAndSgstAtHalfRate() {
        CalcResult result = calculator.calculate(List.of(line("120", "250", "5", "5", "0")), false);

        assertThat(result.lines().get(0).grossAmount()).isEqualByComparingTo("30000.00");
        assertThat(result.lines().get(0).discountAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.lines().get(0).taxableAmount()).isEqualByComparingTo("28500.00");
        assertThat(result.taxes()).extracting(TaxRow::taxType).containsExactly("CGST", "SGST");
        assertThat(result.taxes().get(0).rate()).isEqualByComparingTo("2.5");
        assertThat(result.taxes().get(0).amount()).isEqualByComparingTo("712.50");
        assertThat(result.totals().netAmount()).isEqualByComparingTo("29925.00");
        assertThat(result.totals().roundOff()).isEqualByComparingTo("0.00");
    }

    @Test
    void interStateUsesSingleIgstRow() {
        CalcResult result = calculator.calculate(List.of(line("10", "100", "0", "18", "0")), true);

        assertThat(result.taxes()).hasSize(1);
        assertThat(result.taxes().get(0).taxType()).isEqualTo("IGST");
        assertThat(result.taxes().get(0).amount()).isEqualByComparingTo("180.00");
        assertThat(result.totals().igstAmount()).isEqualByComparingTo("180.00");
        assertThat(result.totals().cgstAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void mixedRatesProduceOneRowPerRateAndType() {
        CalcResult result = calculator.calculate(List.of(
                line("1", "1000", "0", "5", "0"),
                line("1", "2000", "0", "5", "0"),
                line("1", "1000", "0", "18", "0")), false);

        assertThat(result.taxes()).hasSize(4);
        assertThat(result.totals().cgstAmount()).isEqualByComparingTo("165.00");
        assertThat(result.totals().sgstAmount()).isEqualByComparingTo("165.00");
    }

    @Test
    void taxIsSummedUnroundedBeforeRounding() {
        // Each line taxable 0.10 at 9% CGST = 0.009; 10 lines = 0.09 (rounding per line would give 0.00)
        List<LineInput> lines = java.util.Collections.nCopies(10, line("1", "0.10", "0", "18", "0"));

        CalcResult result = calculator.calculate(lines, false);

        assertThat(result.totals().cgstAmount()).isEqualByComparingTo("0.09");
    }

    @Test
    void netIsRoundedToRupeeWithSignedRoundOff() {
        CalcResult up = calculator.calculate(List.of(line("1", "100.60", "0", "0", "0")), false);
        CalcResult down = calculator.calculate(List.of(line("1", "100.40", "0", "0", "0")), false);

        assertThat(up.totals().netAmount()).isEqualByComparingTo("101.00");
        assertThat(up.totals().roundOff()).isEqualByComparingTo("0.40");
        assertThat(down.totals().netAmount()).isEqualByComparingTo("100.00");
        assertThat(down.totals().roundOff()).isEqualByComparingTo("-0.40");
    }

    @Test
    void cessGetsItsOwnRowAndZeroTaxRowsAreSkipped() {
        CalcResult result = calculator.calculate(List.of(line("1", "1000", "0", "28", "12")), true);

        assertThat(result.taxes()).extracting(TaxRow::taxType).containsExactly("IGST", "CESS");
        assertThat(result.totals().cessAmount()).isEqualByComparingTo("120.00");

        CalcResult zeroRated = calculator.calculate(List.of(line("1", "1000", "0", "0", "0")), false);
        assertThat(zeroRated.taxes()).isEmpty();
    }
}
