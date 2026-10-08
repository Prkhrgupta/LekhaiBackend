package in.lekhai.voucher.mapper;

import java.time.LocalDate;

import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherType;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VoucherPostingMapperTest {

    private final VoucherPostingMapper mapper = new VoucherPostingMapper();

    @Test
    void legacyResponse_keepsTypeDashNumberWithoutWarnings() {
        VoucherResponse actual = mapper.mapToCommonVoucherResponse(
                new Voucher(VoucherType.PAYMENT, 7L, LocalDate.of(2025, 6, 15), "payout"));

        assertThat(actual.getVoucherNumber()).isEqualTo("PY-7");
        assertThat(actual.getWarnings()).isNull();
    }

    @Test
    void seriesResponse_carriesDisplayAndGapWarning() {
        Voucher voucher = new Voucher(
                VoucherType.PAYMENT, 43L, LocalDate.of(2025, 6, 15), "payout");
        voucher.setId(1L);

        VoucherResponse actual = mapper.mapToCommonVoucherResponse(voucher, "CASH/43", 5L);

        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getVoucherNumber()).isEqualTo("CASH/43");
        assertThat(actual.getWarnings().getFirstMissingBillNo().get()).isEqualTo(5L);
    }

    @Test
    void seriesResponse_withoutGapOmitsWarnings() {
        VoucherResponse actual = mapper.mapToCommonVoucherResponse(
                new Voucher(VoucherType.RECEIPT, 10L, LocalDate.of(2025, 6, 15), "receipt"),
                "R/10", null);

        assertThat(actual.getVoucherNumber()).isEqualTo("R/10");
        assertThat(actual.getWarnings()).isNull();
    }
}
