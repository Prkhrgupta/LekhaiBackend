package in.lekhai.voucher.service.posting;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import in.lekhai.authentication.model.JwtClaims;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.error.controller.voucher.exception.SeriesFyMissingException;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.dto.posting.PostingResult;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import in.lekhai.voucher.service.SeriesReserveService;
import in.lekhai.voucher.service.VoucherCounterService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherPostingServiceTest {

    @Mock
    private VoucherCounterService voucherCounterService;
    @Mock
    private SeriesReserveService seriesReserveService;
    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private VoucherEntryRepository voucherEntryRepository;
    @Mock
    private LedgerRepository ledgerRepository;

    private VoucherPostingService postingService;

    @BeforeEach
    void setUp() {
        postingService = new VoucherPostingService(
                voucherCounterService,
                seriesReserveService,
                new VoucherPostingMapper(),
                voucherRepository,
                voucherEntryRepository,
                ledgerRepository);
        when(ledgerRepository.existsById(any())).thenReturn(true);
        lenient().when(voucherRepository.save(any())).thenAnswer(invocation -> {
            Voucher voucher = invocation.getArgument(0);
            voucher.setId(1L);
            return voucher;
        });
    }

    @Test
    void post_withoutSeries_usesCounterWithLegacyDisplay() {
        try (var jwt = mockStatic(JwtUtil.class)) {
            jwt.when(JwtUtil::extractJwtClaim)
                    .thenReturn(new JwtClaims(null, null, null, null, Year.of(2025)));
            when(voucherCounterService.reserveNextNumber(VoucherType.PAYMENT)).thenReturn(7L);

            PostingResult actual = postingService.post(
                    new PostingRequest(VoucherType.PAYMENT, LocalDate.of(2025, 6, 15),
                            "payout", balancedEntries()));

            assertThat(actual.displayVoucherNumber()).isEqualTo("PY-7");
            assertThat(actual.firstMissingBillNo()).isNull();
            assertThat(actual.voucher().getSeriesId()).isNull();
            assertThat(actual.voucher().getFinancialYear()).isNull();
            verify(seriesReserveService, never()).reserve(any(), any(), any(), any());
        }
    }

    @Test
    void post_withSeriesAuto_persistsSeriesLinkAndWarning() {
        try (var jwt = mockStatic(JwtUtil.class)) {
            jwt.when(JwtUtil::extractJwtClaim)
                    .thenReturn(new JwtClaims(null, null, null, null, Year.of(2025)));
            when(seriesReserveService.reserve(
                    VoucherType.PAYMENT, "CASH-1", LocalDate.of(2025, 6, 15), null))
                    .thenReturn(new SeriesReserveService.ReserveResult(43L, 5L, 9L, "CASH/", "/25"));

            PostingResult actual = postingService.post(
                    new PostingRequest(VoucherType.PAYMENT, LocalDate.of(2025, 6, 15),
                            "payout", balancedEntries(), "CASH-1", null));

            assertThat(actual.displayVoucherNumber()).isEqualTo("CASH/43/25");
            assertThat(actual.firstMissingBillNo()).isEqualTo(5L);
            assertThat(actual.voucher().getVoucherNumber()).isEqualTo(43L);
            assertThat(actual.voucher().getSeriesId()).isEqualTo(9L);
            assertThat(actual.voucher().getFinancialYear()).isEqualTo("2025-26");
            verify(voucherCounterService, never()).reserveNextNumber(any());
        }
    }

    @Test
    void post_withSeriesManual_passesManualBillNo() {
        try (var jwt = mockStatic(JwtUtil.class)) {
            jwt.when(JwtUtil::extractJwtClaim)
                    .thenReturn(new JwtClaims(null, null, null, null, Year.of(2025)));
            when(seriesReserveService.reserve(
                    VoucherType.RECEIPT, "RCPT-1", LocalDate.of(2025, 6, 15), 10L))
                    .thenReturn(new SeriesReserveService.ReserveResult(10L, null, 4L, "R/", null));

            PostingResult actual = postingService.post(
                    new PostingRequest(VoucherType.RECEIPT, LocalDate.of(2025, 6, 15),
                            "receipt", balancedEntries(), "RCPT-1", 10L));

            assertThat(actual.displayVoucherNumber()).isEqualTo("R/10");
            assertThat(actual.firstMissingBillNo()).isNull();
        }
    }

    @Test
    void post_withUnknownSeries_propagatesFyMissing() {
        try (var jwt = mockStatic(JwtUtil.class)) {
            jwt.when(JwtUtil::extractJwtClaim)
                    .thenReturn(new JwtClaims(null, null, null, null, Year.of(2025)));
            when(seriesReserveService.reserve(any(), any(), any(), any()))
                    .thenThrow(new SeriesFyMissingException("PAYMENT", "2025-26"));

            assertThatThrownBy(() -> postingService.post(
                    new PostingRequest(VoucherType.PAYMENT, LocalDate.of(2025, 6, 15),
                            "payout", balancedEntries(), "NOPE", null)))
                    .isInstanceOf(SeriesFyMissingException.class);
        }
    }

    private static List<PostingEntry> balancedEntries() {
        return List.of(
                new PostingEntry(20L, new BigDecimal("500"), BigDecimal.ZERO, null),
                new PostingEntry(10L, BigDecimal.ZERO, new BigDecimal("500"), null));
    }
}
