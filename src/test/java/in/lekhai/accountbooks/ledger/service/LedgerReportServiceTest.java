package in.lekhai.accountbooks.ledger.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.AccountLedgerEntryItem;
import in.lekhai.contract.model.AccountLedgerPageResponse;
import in.lekhai.contract.model.PaginationMeta;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.repository.LedgerEntryWithBalanceProjection;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LedgerReportServiceTest {

    @Mock
    private LedgerRepository ledgerRepository;
    @Mock
    private VoucherEntryRepository voucherEntryRepository;
    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private LedgerReportService ledgerReportService;

    @BeforeEach
    void setUp() {
        lenient().when(voucherEntryRepository.sumNetBefore(anyLong(), any()))
                .thenReturn(BigDecimal.ZERO);
        lenient().when(voucherEntryRepository.countByLedgerIdAndDateBetween(anyLong(), any(), any()))
                .thenReturn(0L);
    }

    @Test
    void page0_showsOpeningBalanceRow() {
        Ledger ledger = ledger(AccountEntryType.DR, 1000);
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(ledger));

        LedgerEntryWithBalanceProjection entry = projection(10L, 100L, 1L, 1,
                200, null, LocalDate.of(2026, 5, 1), 200);
        when(voucherEntryRepository.findPageWithRunningBalance(eq(1L), any(), any(), eq(10), eq(0)))
                .thenReturn(List.of(entry));
        when(voucherRepository.findAllById(List.of(100L)))
                .thenReturn(List.of(voucher(100L, LocalDate.of(2026, 5, 1))));
        when(voucherEntryRepository.findByVoucherIdIn(List.of(100L)))
                .thenReturn(List.of(new VoucherEntry(100L, 2L, 1, null, BigDecimal.valueOf(200), null)));

        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), PageRequest.of(0, 10));

        assertEquals(2, response.getData().size());
        AccountLedgerEntryItem opening = response.getData().get(0);
        assertEquals("Opening Balance", opening.getLedgerName());
        assertEquals(AccountEntryType.DR, opening.getCrdr());
        assertEquals(0, opening.getDebitAmt().compareTo(BigDecimal.valueOf(1000)));

        AccountLedgerEntryItem entryItem = response.getData().get(1);
        assertEquals(0, entryItem.getBalance().compareTo(BigDecimal.valueOf(1200)));
        assertEquals(AccountEntryType.DR, entryItem.getCrdr());
        assertEquals("2026-05-01", entryItem.getDate());
    }

    @Test
    void openingBalanceNotShownOnPageBeyondZero() {
        Ledger ledger = ledger(AccountEntryType.DR, 1000);
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(ledger));

        LedgerEntryWithBalanceProjection entry = projection(10L, 100L, 1L, 1,
                200, null, LocalDate.of(2026, 5, 1), 200);
        when(voucherEntryRepository.findPageWithRunningBalance(eq(1L), any(), any(), eq(10), eq(10)))
                .thenReturn(List.of(entry));
        when(voucherRepository.findAllById(List.of(100L)))
                .thenReturn(List.of(voucher(100L, LocalDate.of(2026, 5, 1))));
        when(voucherEntryRepository.findByVoucherIdIn(List.of(100L)))
                .thenReturn(List.of());

        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), PageRequest.of(1, 10));

        assertEquals(1, response.getData().size());
        assertNotEquals("Opening Balance", response.getData().get(0).getLedgerName());
    }

    @Test
    void creditOpeningBalanceIsNegatedForBalanceMath() {
        Ledger ledger = ledger(AccountEntryType.CR, 500);
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(ledger));

        LedgerEntryWithBalanceProjection entry = projection(10L, 100L, 1L, 1,
                800, null, LocalDate.of(2026, 5, 1), 800);
        when(voucherEntryRepository.findPageWithRunningBalance(eq(1L), any(), any(), eq(10), eq(0)))
                .thenReturn(List.of(entry));
        when(voucherRepository.findAllById(List.of(100L)))
                .thenReturn(List.of(voucher(100L, LocalDate.of(2026, 5, 1))));
        when(voucherEntryRepository.findByVoucherIdIn(List.of(100L)))
                .thenReturn(List.of());

        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), PageRequest.of(0, 10));

        AccountLedgerEntryItem opening = response.getData().get(0);
        assertEquals(AccountEntryType.CR, opening.getCrdr());
        assertEquals(0, opening.getCreditAmt().compareTo(BigDecimal.valueOf(500)));

        AccountLedgerEntryItem entryItem = response.getData().get(1);
        assertEquals(0, entryItem.getBalance().compareTo(BigDecimal.valueOf(300)));
        assertEquals(AccountEntryType.DR, entryItem.getCrdr());
    }

    @Test
    void negativeRunningBalanceIsReportedAsCredit() {
        Ledger ledger = ledger(AccountEntryType.DR, 100);
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(ledger));

        LedgerEntryWithBalanceProjection entry = projection(10L, 100L, 1L, 1,
                null, 150, LocalDate.of(2026, 5, 1), -150);
        when(voucherEntryRepository.findPageWithRunningBalance(eq(1L), any(), any(), eq(10), eq(0)))
                .thenReturn(List.of(entry));
        when(voucherRepository.findAllById(List.of(100L)))
                .thenReturn(List.of(voucher(100L, LocalDate.of(2026, 5, 1))));
        when(voucherEntryRepository.findByVoucherIdIn(List.of(100L)))
                .thenReturn(List.of());

        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), PageRequest.of(0, 10));

        AccountLedgerEntryItem entryItem = response.getData().get(1);
        assertEquals(0, entryItem.getBalance().compareTo(BigDecimal.valueOf(50)));
        assertEquals(AccountEntryType.CR, entryItem.getCrdr());
    }

    @Test
    void multiDrPaymentVoucherListsAllContraEntries() {
        Ledger ledger = ledger(AccountEntryType.DR, 0);
        when(ledgerRepository.findById(3L)).thenReturn(Optional.of(ledger));

        LedgerEntryWithBalanceProjection entry = projection(30L, 100L, 3L, 3,
                null, 8000, LocalDate.of(2026, 5, 1), -8000);
        when(voucherEntryRepository.findPageWithRunningBalance(eq(3L), any(), any(), eq(10), eq(0)))
                .thenReturn(List.of(entry));
        when(voucherRepository.findAllById(List.of(100L)))
                .thenReturn(List.of(voucher(100L, LocalDate.of(2026, 5, 1))));
        when(voucherEntryRepository.findByVoucherIdIn(List.of(100L)))
                .thenReturn(List.of(
                        new VoucherEntry(100L, 5L, 1, BigDecimal.valueOf(5000), null, null),
                        new VoucherEntry(100L, 6L, 2, BigDecimal.valueOf(3000), null, null),
                        new VoucherEntry(100L, 3L, 3, null, BigDecimal.valueOf(8000), null)));
        when(ledgerRepository.findAllById(anySet())).thenReturn(List.of(
                namedLedger(5L, "Rent"),
                namedLedger(6L, "Electricity")));

        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                3L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), PageRequest.of(0, 10));

        AccountLedgerEntryItem item = response.getData().get(0);
        assertEquals("Rent, Electricity", item.getLedgerName());
        assertEquals(2, item.getContraEntries().size());
        assertEquals("Rent", item.getContraEntries().get(0).getLedgerName());
        assertEquals(0, item.getContraEntries().get(0).getDebitAmt().compareTo(BigDecimal.valueOf(5000)));
        assertEquals("Electricity", item.getContraEntries().get(1).getLedgerName());
        assertEquals(0, item.getContraEntries().get(1).getDebitAmt().compareTo(BigDecimal.valueOf(3000)));
    }

    @Test
    void singleContraEntryIsNotReported() {
        Ledger ledger = ledger(AccountEntryType.DR, 0);
        when(ledgerRepository.findById(5L)).thenReturn(Optional.of(ledger));

        LedgerEntryWithBalanceProjection entry = projection(30L, 100L, 5L, 1,
                5000, null, LocalDate.of(2026, 5, 1), 5000);
        when(voucherEntryRepository.findPageWithRunningBalance(eq(5L), any(), any(), eq(10), eq(0)))
                .thenReturn(List.of(entry));
        when(voucherRepository.findAllById(List.of(100L)))
                .thenReturn(List.of(voucher(100L, LocalDate.of(2026, 5, 1))));
        when(voucherEntryRepository.findByVoucherIdIn(List.of(100L)))
                .thenReturn(List.of(
                        new VoucherEntry(100L, 5L, 1, BigDecimal.valueOf(5000), null, null),
                        new VoucherEntry(100L, 6L, 2, BigDecimal.valueOf(3000), null, null),
                        new VoucherEntry(100L, 3L, 3, null, BigDecimal.valueOf(8000), null)));
        when(ledgerRepository.findAllById(anySet())).thenReturn(List.of(
                namedLedger(3L, "Bank")));

        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                5L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), PageRequest.of(0, 10));

        AccountLedgerEntryItem item = response.getData().get(0);
        assertEquals("", item.getLedgerName());
        assertTrue(item.getContraEntries().isEmpty());
    }

    @Test
    void nullDatesDefaultToFinancialYearFromJwt() {
        Ledger ledger = ledger(AccountEntryType.DR, 0);
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(ledger));

        try (var jwt = mockStatic(JwtUtil.class)) {
            jwt.when(JwtUtil::extractJwtClaim)
                    .thenReturn(new in.lekhai.authentication.model.JwtClaims(
                            null, null, null, null, Year.of(2026)));

            ledgerReportService.getAccountLedgerEntries(
                    1L, null, null, PageRequest.of(0, 10));
        }

        verify(voucherEntryRepository).findPageWithRunningBalance(
                eq(1L),
                eq(LocalDate.of(2026, 4, 1)),
                eq(LocalDate.of(2027, 3, 31)),
                eq(10), eq(0));
        verify(voucherEntryRepository).sumNetBefore(eq(1L), eq(LocalDate.of(2026, 4, 1)));
        verify(voucherEntryRepository).countByLedgerIdAndDateBetween(
                eq(1L), eq(LocalDate.of(2026, 4, 1)), eq(LocalDate.of(2027, 3, 31)));
    }

    @Test
    void emptyLedgerReturnsZeroPagination() {
        Ledger ledger = ledger(AccountEntryType.DR, 0);
        when(ledgerRepository.findById(1L)).thenReturn(Optional.of(ledger));
        when(voucherEntryRepository.findPageWithRunningBalance(eq(1L), any(), any(), eq(10), eq(0)))
                .thenReturn(List.of());

        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), PageRequest.of(0, 10));

        assertTrue(response.getData().isEmpty());
        PaginationMeta pagination = response.getPagination();
        assertEquals(0, pagination.getTotalPages());
        assertEquals(0L, pagination.getTotalElements());
    }

    @Test
    void fromDateAfterToDateThrows() {
        assertThrows(IllegalArgumentException.class, () -> ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2027, 3, 31), LocalDate.of(2026, 4, 1), PageRequest.of(0, 10)));
    }

    private Ledger ledger(AccountEntryType type, int opening) {
        return new Ledger("Test Ledger", null, 1L, BigDecimal.valueOf(opening), type,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private Ledger namedLedger(long id, String name) {
        Ledger ledger = new Ledger(name, null, 1L, BigDecimal.ZERO, AccountEntryType.DR,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger.setId(id);
        return ledger;
    }

    private Voucher voucher(long id, LocalDate date) {
        Voucher voucher = new Voucher(VoucherType.SALES, 1L, date, "narration");
        voucher.setId(id);
        return voucher;
    }

    private LedgerEntryWithBalanceProjection projection(long id, long voucherId, long ledgerId, int line,
                                                        Integer debit, Integer credit, LocalDate date, int runningNet) {
        return new LedgerEntryWithBalanceProjection(
                id, voucherId, ledgerId, line,
                debit != null ? BigDecimal.valueOf(debit) : null,
                credit != null ? BigDecimal.valueOf(credit) : null,
                date, BigDecimal.valueOf(runningNet));
    }
}
