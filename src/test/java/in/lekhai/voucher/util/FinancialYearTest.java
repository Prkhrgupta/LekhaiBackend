package in.lekhai.voucher.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialYearTest {

    @Test
    void marchDateBelongsToPreviousFinancialYear() {
        assertThat(FinancialYear.from(LocalDate.of(2027, 3, 4))).isEqualTo("2026-27");
    }

    @Test
    void aprilFirstStartsNewFinancialYear() {
        assertThat(FinancialYear.from(LocalDate.of(2026, 4, 1))).isEqualTo("2026-27");
    }

    @Test
    void marchThirtyFirstStaysInPreviousFinancialYear() {
        assertThat(FinancialYear.from(LocalDate.of(2027, 3, 31))).isEqualTo("2026-27");
    }

    @Test
    void validatesFormatAndRollover() {
        assertThat(FinancialYear.isValid("2025-26")).isTrue();
        assertThat(FinancialYear.isValid("1999-00")).isTrue();
        assertThat(FinancialYear.isValid("2025-27")).isFalse();
        assertThat(FinancialYear.isValid("25-26")).isFalse();
        assertThat(FinancialYear.isValid(null)).isFalse();
    }
}
