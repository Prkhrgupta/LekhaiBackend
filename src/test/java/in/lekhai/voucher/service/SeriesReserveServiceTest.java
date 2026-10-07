package in.lekhai.voucher.service;

import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.error.controller.voucher.exception.DuplicateBillNumberException;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.repository.CustomVoucherSeriesRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeriesReserveServiceTest {

    @Mock
    private CustomVoucherSeriesRepository seriesRepository;
    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private SeriesReserveService reserveService;

    @Test
    void autoAllocate_returnsNextAndAdvancesCounter() {
        in.lekhai.voucher.entity.CustomVoucherSeries series = storedSeries(1L, 5L, true, "2026-27");
        when(seriesRepository.findForUpdate(VoucherType.SALES, "GST", "2026-27"))
                .thenReturn(Optional.of(series));
        when(seriesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(voucherRepository.findBillNumbersBySeriesAndFy(1L, "2026-27"))
                .thenReturn(List.of(1L, 2L, 3L, 4L));

        SeriesReserveService.ReserveResult actual = reserveService.reserveAuto(
                VoucherType.SALES, "GST", LocalDate.of(2027, 3, 4));

        assertThat(actual.billNo()).isEqualTo(5L);
        assertThat(series.getNextNumber()).isEqualTo(6L);
        assertThat(actual.firstMissingBillNo()).isNull();
    }

    @Test
    void marchDateConsumesPreviousFinancialYearRow() {
        in.lekhai.voucher.entity.CustomVoucherSeries series = storedSeries(1L, 1L, true, "2026-27");
        when(seriesRepository.findForUpdate(VoucherType.SALES, "GST", "2026-27"))
                .thenReturn(Optional.of(series));
        when(seriesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(voucherRepository.findBillNumbersBySeriesAndFy(1L, "2026-27"))
                .thenReturn(List.of());

        reserveService.reserveAuto(VoucherType.SALES, "GST", LocalDate.of(2027, 3, 4));

        assertThat(series.getFinancialYear()).isEqualTo("2026-27");
    }

    @Test
    void manualForward_bumpsCounterToManualPlusOne() {
        in.lekhai.voucher.entity.CustomVoucherSeries series = storedSeries(1L, 5L, true, "2025-26");
        when(seriesRepository.findForUpdate(VoucherType.SALES, "GST", "2025-26"))
                .thenReturn(Optional.of(series));
        when(seriesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(voucherRepository.existsBySeriesAndFyAndNumber(1L, "2025-26", 10L))
                .thenReturn(false);
        when(voucherRepository.findBillNumbersBySeriesAndFy(1L, "2025-26"))
                .thenReturn(List.of(1L, 2L, 3L, 4L));

        SeriesReserveService.ReserveResult actual = reserveService.reserve(
                VoucherType.SALES, "GST", LocalDate.of(2025, 6, 15), 10L);

        assertThat(actual.billNo()).isEqualTo(10L);
        assertThat(series.getNextNumber()).isEqualTo(11L);
    }

    @Test
    void backfill_keepsCounterAndWarnsFirstMissing() {
        in.lekhai.voucher.entity.CustomVoucherSeries series = storedSeries(1L, 6L, true, "2025-26");
        when(seriesRepository.findForUpdate(VoucherType.SALES, "GST", "2025-26"))
                .thenReturn(Optional.of(series));
        when(voucherRepository.existsBySeriesAndFyAndNumber(1L, "2025-26", 3L))
                .thenReturn(false);
        when(voucherRepository.findBillNumbersBySeriesAndFy(1L, "2025-26"))
                .thenReturn(List.of(1L, 2L, 4L, 5L));

        SeriesReserveService.ReserveResult actual = reserveService.reserve(
                VoucherType.SALES, "GST", LocalDate.of(2025, 6, 15), 3L);

        assertThat(actual.billNo()).isEqualTo(3L);
        assertThat(series.getNextNumber()).isEqualTo(6L);
        assertThat(actual.firstMissingBillNo()).isEqualTo(3L);
    }

    @Test
    void duplicateManual_isRejected() {
        in.lekhai.voucher.entity.CustomVoucherSeries series = storedSeries(1L, 6L, true, "2025-26");
        when(seriesRepository.findForUpdate(VoucherType.SALES, "GST", "2025-26"))
                .thenReturn(Optional.of(series));
        when(voucherRepository.existsBySeriesAndFyAndNumber(1L, "2025-26", 4L))
                .thenReturn(true);

        assertThatThrownBy(() -> reserveService.reserve(
                VoucherType.SALES, "GST", LocalDate.of(2025, 6, 15), 4L))
                .isInstanceOf(DuplicateBillNumberException.class);
    }

    @Test
    void missingSeriesRow_throwsFyMissing() {
        when(seriesRepository.findForUpdate(VoucherType.SALES, "GST", "2025-26"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reserveService.reserveAuto(
                VoucherType.SALES, "GST", LocalDate.of(2025, 6, 15)))
                .isInstanceOf(SeriesFyMissingException.class)
                .satisfies(e -> assertThat(
                        ((SeriesFyMissingException) e).getStatusCode().value()).isEqualTo(422));
    }

    @Test
    void manualNotAllowed_isRejected() {
        in.lekhai.voucher.entity.CustomVoucherSeries series = storedSeries(1L, 5L, false, "2025-26");
        when(seriesRepository.findForUpdate(VoucherType.SALES, "GST", "2025-26"))
                .thenReturn(Optional.of(series));

        assertThatThrownBy(() -> reserveService.reserve(
                VoucherType.SALES, "GST", LocalDate.of(2025, 6, 15), 7L))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("Manual bill entry");
    }

    private static in.lekhai.voucher.entity.CustomVoucherSeries storedSeries(
            Long id, Long next, boolean allowManual, String fy) {
        in.lekhai.voucher.entity.CustomVoucherSeries series =
                new in.lekhai.voucher.entity.CustomVoucherSeries();
        series.setId(id);
        series.setVoucherType(VoucherType.SALES);
        series.setSeriesCode("GST");
        series.setFinancialYear(fy);
        series.setPrefix("GST/");
        series.setSuffix("/25");
        series.setAllowManualEntry(allowManual);
        series.setNextNumber(next);
        return series;
    }
}
