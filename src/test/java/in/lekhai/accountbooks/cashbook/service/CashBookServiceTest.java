package in.lekhai.accountbooks.cashbook.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import in.lekhai.voucher.repository.LedgerEntryWithBalanceProjection;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import in.lekhai.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashBookServiceTest {

    @Mock
    private AccountGroupRepository accountGroupRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private VoucherEntryRepository voucherEntryRepository;

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private CashBookService cashBookService;

    private AccountGroup cashGroup;
    private Ledger cashLedger;
    private Ledger salesLedger;
    private Ledger rentLedger;

    @BeforeEach
    void setUp() {
        cashGroup = new AccountGroup("Cash in Hand", 6L, "ASSET", in.lekhai.common.AccountEntryType.DR, false);
        cashGroup.setId(29L);

        cashLedger = createLedger(21L, "Main Cash A/c", 29L, BigDecimal.valueOf(10000), AccountEntryType.DR);
        salesLedger = createLedger(26L, "Domestic Sales A/c", 11L, BigDecimal.ZERO, AccountEntryType.CR);
        rentLedger = createLedger(27L, "Factory Rent", 13L, BigDecimal.ZERO, AccountEntryType.DR);

        lenient().when(accountGroupRepository.findAll()).thenReturn(List.of(cashGroup));
        lenient().when(ledgerRepository.findByAccountGroupIdIn(anySet())).thenReturn(List.of(cashLedger));
        lenient().when(ledgerRepository.findById(21L)).thenReturn(Optional.of(cashLedger));
        lenient().when(ledgerRepository.findAllById(any())).thenReturn(List.of(cashLedger, salesLedger, rentLedger));
    }

    private Ledger createLedger(Long id, String name, Long groupId, BigDecimal opening, AccountEntryType type) {
        Ledger ledger = new Ledger(name, null, groupId, opening, type,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger.setId(id);
        ledger.setActive(true);
        return ledger;
    }

    @Test
    void testGetCashBook_OpeningOnly() {
        LocalDate fromDate = LocalDate.of(2026, 4, 1);
        LocalDate toDate = LocalDate.of(2026, 4, 30);

        when(voucherEntryRepository.sumNetBeforeByLedgerIds(anyList(), eq(fromDate)))
                .thenReturn(BigDecimal.ZERO);
        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(anyList(), eq(fromDate), eq(toDate)))
                .thenReturn(List.of());

        CashBookResponse response = cashBookService.getCashBook(
                fromDate, toDate, "DETAIL", false, false, "DAILY", null);

        assertNotNull(response);
        assertEquals(new BigDecimal("10000"), response.getOpeningBalance());
        assertEquals(AccountEntryType.DR, response.getOpeningBalanceType());
        assertEquals(BigDecimal.ZERO, response.getTotalReceived());
        assertEquals(BigDecimal.ZERO, response.getTotalPayment());
        assertEquals(new BigDecimal("10000"), response.getClosingBalance());
        assertEquals(AccountEntryType.DR, response.getClosingBalanceType());

        // Opening and Final Closing entries
        assertEquals(2, response.getEntries().size());
        assertEquals("OPENING", response.getEntries().get(0).getEntryType());
        assertEquals("CLOSING", response.getEntries().get(1).getEntryType());
    }

    @Test
    void testGetCashBook_WithDailyTransactions() {
        LocalDate fromDate = LocalDate.of(2026, 6, 1);
        LocalDate toDate = LocalDate.of(2026, 6, 30);

        when(voucherEntryRepository.sumNetBeforeByLedgerIds(anyList(), eq(fromDate)))
                .thenReturn(BigDecimal.ZERO);

        LocalDate txDate = LocalDate.of(2026, 6, 15);
        LedgerEntryWithBalanceProjection entry1 = new LedgerEntryWithBalanceProjection(
                1L, 101L, 21L, 1, BigDecimal.valueOf(5000), BigDecimal.ZERO, txDate, BigDecimal.ZERO);
        LedgerEntryWithBalanceProjection entry2 = new LedgerEntryWithBalanceProjection(
                2L, 102L, 21L, 1, BigDecimal.ZERO, BigDecimal.valueOf(2000), txDate, BigDecimal.ZERO);

        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(anyList(), eq(fromDate), eq(toDate)))
                .thenReturn(List.of(entry1, entry2));

        Voucher v1 = new Voucher(VoucherType.RECEIPT, 1L, txDate, "Cash received from sales");
        v1.setId(101L);
        Voucher v2 = new Voucher(VoucherType.PAYMENT, 2L, txDate, "Rent payment in cash");
        v2.setId(102L);
        when(voucherRepository.findAllById(anyList())).thenReturn(List.of(v1, v2));

        VoucherEntry ve1Contra = new VoucherEntry(101L, 26L, 2, BigDecimal.ZERO, BigDecimal.valueOf(5000), "");
        VoucherEntry ve2Contra = new VoucherEntry(102L, 27L, 2, BigDecimal.valueOf(2000), BigDecimal.ZERO, "");
        when(voucherEntryRepository.findByVoucherIdIn(anyList())).thenReturn(List.of(ve1Contra, ve2Contra));

        CashBookResponse response = cashBookService.getCashBook(
                fromDate, toDate, "DETAIL", false, false, "DAILY", null);

        assertNotNull(response);
        assertEquals(new BigDecimal("10000"), response.getOpeningBalance());
        assertEquals(new BigDecimal("5000"), response.getTotalReceived());
        assertEquals(new BigDecimal("2000"), response.getTotalPayment());
        assertEquals(new BigDecimal("13000"), response.getClosingBalance());
        assertEquals(AccountEntryType.DR, response.getClosingBalanceType());

        // Check entries: Opening + 2 Txns + Daily Closing + Final Closing
        assertEquals(5, response.getEntries().size());
        assertEquals("OPENING", response.getEntries().get(0).getEntryType());
        assertEquals("TRANSACTION", response.getEntries().get(1).getEntryType());
        assertEquals("TRANSACTION", response.getEntries().get(2).getEntryType());
        assertEquals("DAILY_CLOSING", response.getEntries().get(3).getEntryType());
        assertEquals("CLOSING", response.getEntries().get(4).getEntryType());
    }

    @Test
    void testGetCashBook_SingleCashSaleAggregation() {
        LocalDate fromDate = LocalDate.of(2026, 6, 1);
        LocalDate toDate = LocalDate.of(2026, 6, 30);

        when(voucherEntryRepository.sumNetBeforeByLedgerIds(anyList(), eq(fromDate)))
                .thenReturn(BigDecimal.ZERO);

        LocalDate txDate = LocalDate.of(2026, 6, 10);
        LedgerEntryWithBalanceProjection sale1 = new LedgerEntryWithBalanceProjection(
                1L, 201L, 21L, 1, BigDecimal.valueOf(3000), BigDecimal.ZERO, txDate, BigDecimal.ZERO);
        LedgerEntryWithBalanceProjection sale2 = new LedgerEntryWithBalanceProjection(
                2L, 202L, 21L, 1, BigDecimal.valueOf(4500), BigDecimal.ZERO, txDate, BigDecimal.ZERO);

        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(anyList(), eq(fromDate), eq(toDate)))
                .thenReturn(List.of(sale1, sale2));

        Voucher v1 = new Voucher(VoucherType.SALES, 10L, txDate, "Cash sale 1");
        v1.setId(201L);
        Voucher v2 = new Voucher(VoucherType.SALES, 11L, txDate, "Cash sale 2");
        v2.setId(202L);
        when(voucherRepository.findAllById(anyList())).thenReturn(List.of(v1, v2));

        CashBookResponse response = cashBookService.getCashBook(
                fromDate, toDate, "DETAIL", true, false, "DAILY", null);

        assertNotNull(response);
        // The two cash sales should be combined into 1 aggregated Cash Sales entry
        List<CashBookEntryItem> txEntries = response.getEntries().stream()
                .filter(e -> "TRANSACTION".equals(e.getEntryType()))
                .toList();

        assertEquals(1, txEntries.size());
        assertEquals("Cash Sales", txEntries.get(0).getParticulars());
        assertEquals(new BigDecimal("7500"), txEntries.get(0).getReceived());
        assertEquals("SALES", txEntries.get(0).getVchType());
    }

    @Test
    void testGetCashBook_MonthlyIntervalAndHideDate() {
        LocalDate fromDate = LocalDate.of(2026, 4, 1);
        LocalDate toDate = LocalDate.of(2026, 5, 31);

        when(voucherEntryRepository.sumNetBeforeByLedgerIds(anyList(), eq(fromDate)))
                .thenReturn(BigDecimal.ZERO);

        LocalDate txDate = LocalDate.of(2026, 4, 15);
        LedgerEntryWithBalanceProjection entry = new LedgerEntryWithBalanceProjection(
                1L, 301L, 21L, 1, BigDecimal.valueOf(1000), BigDecimal.ZERO, txDate, BigDecimal.ZERO);

        when(voucherEntryRepository.findEntriesByLedgerIdsBetweenDates(anyList(), eq(fromDate), eq(toDate)))
                .thenReturn(List.of(entry));

        Voucher v = new Voucher(VoucherType.RECEIPT, 5L, txDate, "Payment received");
        v.setId(301L);
        when(voucherRepository.findAllById(anyList())).thenReturn(List.of(v));

        CashBookResponse response = cashBookService.getCashBook(
                fromDate, toDate, "SUMMARY", false, true, "MONTHLY", null);

        assertNotNull(response);
        // Verify hideDate nullified dates on entries
        assertTrue(response.getEntries().stream().allMatch(e -> e.getDate() == null));
        // Verify monthly closing exists
        assertTrue(response.getEntries().stream().anyMatch(e -> "MONTHLY_CLOSING".equals(e.getEntryType())));
    }
}
