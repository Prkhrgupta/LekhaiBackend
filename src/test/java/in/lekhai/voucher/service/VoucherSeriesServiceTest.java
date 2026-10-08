package in.lekhai.voucher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.lekhai.contract.model.CreateSeriesRequest;
import in.lekhai.contract.model.CustomVoucherSeriesListResponse;
import in.lekhai.contract.model.UpdateSeriesRequest;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.error.controller.voucher.exception.SeriesAlreadyExistsException;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;
import in.lekhai.error.controller.voucher.exception.SeriesImmutableException;
import in.lekhai.error.controller.voucher.exception.SeriesNotFoundException;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.repository.CustomVoucherSeriesRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherSeriesServiceTest {

    @Mock
    private CustomVoucherSeriesRepository seriesRepository;
    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private VoucherSeriesService seriesService;

    @Test
    void createSeries_persistsSalesSeriesWithZeroCurrentBill() {
        when(seriesRepository.existsByVoucherTypeAndSeriesCodeAndFinancialYear(
                VoucherType.SALES, "GST", "2025-26")).thenReturn(false);
        when(seriesRepository.save(any())).thenAnswer(invocation -> {
            in.lekhai.voucher.entity.CustomVoucherSeries series = invocation.getArgument(0);
            series.setId(1L);
            return series;
        });

        in.lekhai.contract.model.CustomVoucherSeries actual = seriesService.createSeries(
                new CreateSeriesRequest("GST", "SALES", "2025-26").prefix("GST/").suffix("/25"));

        assertThat(actual.getId()).isEqualTo(1L);
        assertThat(actual.getSeriesCode()).isEqualTo("GST");
        assertThat(actual.getCurrentBillNo()).isEqualTo(0L);
    }

    @Test
    void createSeries_acceptsAllSupportedTypes() {
        when(seriesRepository.existsByVoucherTypeAndSeriesCodeAndFinancialYear(
                any(), any(), any())).thenReturn(false);
        when(seriesRepository.save(any())).thenAnswer(invocation -> {
            in.lekhai.voucher.entity.CustomVoucherSeries series = invocation.getArgument(0);
            series.setId(1L);
            return series;
        });

        for (String type : List.of("SALES", "PAYMENT", "RECEIPT", "CONTRA", "JOURNAL")) {
            in.lekhai.contract.model.CustomVoucherSeries actual = seriesService.createSeries(
                    new CreateSeriesRequest("GST", type, "2025-26").prefix("GST/").suffix("/25"));

            assertThat(actual.getVoucherType()).isEqualTo(type);
        }
    }

    @Test
    void createSeries_rejectsUnsupportedType() {
        assertThatThrownBy(() -> seriesService.createSeries(
                new CreateSeriesRequest("GST", "PURCHASE", "2025-26")))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("Unsupported voucher_type");
    }

    @Test
    void createSeries_rejectsUnknownType() {
        assertThatThrownBy(() -> seriesService.createSeries(
                new CreateSeriesRequest("GST", "BOGUS", "2025-26")))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("Unsupported voucher_type");
    }

    @Test
    void createSeries_rejectsBadFinancialYear() {
        assertThatThrownBy(() -> seriesService.createSeries(
                new CreateSeriesRequest("GST", "SALES", "2025")))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("financial_year");
    }

    @Test
    void createSeries_rejectsOver16CharConfiguration() {
        assertThatThrownBy(() -> seriesService.createSeries(
                new CreateSeriesRequest("GST", "SALES", "2025-26")
                        .prefix("VERY-LONG-PREFIX-").suffix("/25")))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("16 characters");
    }

    @Test
    void createSeries_rejectsDuplicate() {
        when(seriesRepository.existsByVoucherTypeAndSeriesCodeAndFinancialYear(
                VoucherType.SALES, "GST", "2025-26")).thenReturn(true);

        assertThatThrownBy(() -> seriesService.createSeries(
                new CreateSeriesRequest("GST", "SALES", "2025-26")))
                .isInstanceOf(SeriesAlreadyExistsException.class);
    }

    @Test
    void listSeries_throwsFyMissingWhenFilterMatchesNothing() {
        when(seriesRepository.findFiltered(any(), any(), any(int.class), any(long.class)))
                .thenReturn(List.of());
        when(seriesRepository.countFiltered(any(), any())).thenReturn(0L);

        assertThatThrownBy(() -> seriesService.listSeries("SALES", "2025-26", 0, 20, null))
                .isInstanceOf(SeriesFyMissingException.class)
                .satisfies(e -> {
                    SeriesFyMissingException ex = (SeriesFyMissingException) e;
                    assertThat(ex.getFinancialYear()).isEqualTo("2025-26");
                    assertThat(ex.getStatusCode().value()).isEqualTo(422);
                });
    }

    @Test
    void listSeries_returnsPageWhenRowsExist() {
        in.lekhai.voucher.entity.CustomVoucherSeries stored = storedSeries(1L);
        when(seriesRepository.findFiltered(any(), any(), any(int.class), any(long.class)))
                .thenReturn(List.of(stored));
        when(seriesRepository.countFiltered(any(), any())).thenReturn(1L);

        CustomVoucherSeriesListResponse actual =
                seriesService.listSeries("SALES", "2025-26", 0, 20, null);

        assertThat(actual.getData()).hasSize(1);
        assertThat(actual.getPagination().getTotalElements()).isEqualTo(1L);
    }

    @Test
    void updateSeries_rejectsUnknownId() {
        when(seriesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seriesService.updateSeries(99L, new UpdateSeriesRequest()))
                .isInstanceOf(SeriesNotFoundException.class);
    }

    @Test
    void updateSeries_blocksPrefixChangeAfterFirstVoucher() {
        in.lekhai.voucher.entity.CustomVoucherSeries stored = storedSeries(7L);
        when(seriesRepository.findById(7L)).thenReturn(Optional.of(stored));
        when(voucherRepository.countBySeriesId(7L)).thenReturn(1L);

        assertThatThrownBy(() -> seriesService.updateSeries(
                7L, new UpdateSeriesRequest().prefix("NEW/")))
                .isInstanceOf(SeriesImmutableException.class);
    }

    @Test
    void updateSeries_allowsMetadataChangeAfterFirstVoucher() {
        in.lekhai.voucher.entity.CustomVoucherSeries stored = storedSeries(7L);
        when(seriesRepository.findById(7L)).thenReturn(Optional.of(stored));
        when(seriesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        in.lekhai.contract.model.CustomVoucherSeries actual = seriesService.updateSeries(
                7L, new UpdateSeriesRequest().allowManualEntry(true));

        assertThat(actual.getAllowManualEntry()).isTrue();
        verify(seriesRepository).save(any());
    }

    private static in.lekhai.voucher.entity.CustomVoucherSeries storedSeries(Long id) {
        in.lekhai.voucher.entity.CustomVoucherSeries series =
                new in.lekhai.voucher.entity.CustomVoucherSeries();
        series.setId(id);
        series.setVoucherType(VoucherType.SALES);
        series.setSeriesCode("GST");
        series.setFinancialYear("2025-26");
        series.setPrefix("GST/");
        series.setSuffix("/25");
        series.setAllowManualEntry(false);
        series.setNextNumber(5L);
        return series;
    }
}
