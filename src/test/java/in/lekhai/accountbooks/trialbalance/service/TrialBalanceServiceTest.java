package in.lekhai.accountbooks.trialbalance.service;

import in.lekhai.accountbooks.balancesheet.repository.LedgerNetTotalProjection;
import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.TrialBalanceGroupItem;
import in.lekhai.contract.model.TrialBalanceResponse;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.voucher.repository.VoucherEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrialBalanceServiceTest {

    @Mock
    private AccountGroupRepository accountGroupRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private VoucherEntryRepository voucherEntryRepository;

    @InjectMocks
    private TrialBalanceService trialBalanceService;

    private AccountGroup capitalGroup;
    private AccountGroup currentAssetGroup;
    private AccountGroup cashGroup;
    private AccountGroup directIncomesGroup;

    @BeforeEach
    void setUp() {
        capitalGroup = createGroup(1L, "Capital Account", null, "LIABILITY", in.lekhai.common.AccountEntryType.CR, true);
        currentAssetGroup = createGroup(6L, "Current Assets", null, "ASSET", in.lekhai.common.AccountEntryType.DR, true);
        cashGroup = createGroup(29L, "Cash in Hand", 6L, "ASSET", in.lekhai.common.AccountEntryType.DR, false);
        directIncomesGroup = createGroup(11L, "Direct Incomes", null, "INCOME", in.lekhai.common.AccountEntryType.CR, true);

        lenient().when(accountGroupRepository.findAll()).thenReturn(List.of(
                capitalGroup, currentAssetGroup, cashGroup, directIncomesGroup
        ));
    }

    @Test
    void testSummaryFormat_calculatesOpeningTransactionsClosingAndBalances() {
        Ledger capital = createLedger(101L, "Owner Capital", 1L, BigDecimal.valueOf(100000), AccountEntryType.CR);
        Ledger cash = createLedger(102L, "Petty Cash", 29L, BigDecimal.valueOf(100000), AccountEntryType.DR);
        Ledger sales = createLedger(103L, "Domestic Sales", 11L, BigDecimal.ZERO, AccountEntryType.CR);

        when(ledgerRepository.findAll()).thenReturn(List.of(capital, cash, sales));
        // No vouchers before period start
        when(voucherEntryRepository.sumNetByLedgerBeforeDate(any())).thenReturn(List.of());
        // Vouchers in period: Cash DR 50,000, Sales CR 50,000
        when(voucherEntryRepository.sumNetByLedgerBetweenDates(any(), any())).thenReturn(List.of(
                new LedgerNetTotalProjection(102L, BigDecimal.valueOf(50000), BigDecimal.ZERO),
                new LedgerNetTotalProjection(103L, BigDecimal.ZERO, BigDecimal.valueOf(50000))
        ));

        TrialBalanceResponse response = trialBalanceService.getTrialBalance(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "SUMMARY",
                false
        );

        assertNotNull(response);
        assertEquals("SUMMARY", response.getFormat());
        assertFalse(response.getWithNilBalance());

        // Opening Totals: Dr = 100k, Cr = 100k, Diff = 0 (BALANCED)
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getTotalOpeningDr()));
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getTotalOpeningCr()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getOpeningDifference()));
        assertEquals("BALANCED", response.getOpeningDifferenceType());

        // Transactions Totals: Dr = 50k, Cr = 50k
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(response.getTotalTransactionsDr()));
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(response.getTotalTransactionsCr()));

        // Closing Totals: Dr = 150k (Cash), Cr = 150k (Capital 100k + Sales 50k), Diff = 0 (BALANCED)
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(response.getTotalClosingDr()));
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(response.getTotalClosingCr()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getClosingDifference()));
        assertEquals("BALANCED", response.getClosingDifferenceType());

        // Summary format check: no nested items
        for (TrialBalanceGroupItem g : response.getGroups()) {
            assertTrue(g.getSubGroups().isEmpty());
            assertTrue(g.getLedgers().isEmpty());
        }
    }

    @Test
    void testDetailFormat_includesHierarchy() {
        Ledger cash = createLedger(102L, "Petty Cash", 29L, BigDecimal.valueOf(25000), AccountEntryType.DR);
        when(ledgerRepository.findAll()).thenReturn(List.of(cash));
        when(voucherEntryRepository.sumNetByLedgerBeforeDate(any())).thenReturn(List.of());
        when(voucherEntryRepository.sumNetByLedgerBetweenDates(any(), any())).thenReturn(List.of());

        TrialBalanceResponse response = trialBalanceService.getTrialBalance(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "DETAIL",
                false
        );

        assertNotNull(response);
        assertEquals("DETAIL", response.getFormat());

        TrialBalanceGroupItem currentAsset = response.getGroups().stream()
                .filter(g -> g.getGroupId().equals(6L))
                .findFirst()
                .orElseThrow();

        assertEquals("Current Assets", currentAsset.getGroupName());
        assertEquals(1, currentAsset.getSubGroups().size());
        assertEquals("Cash in Hand", currentAsset.getSubGroups().get(0).getGroupName());
        assertEquals(1, currentAsset.getSubGroups().get(0).getLedgers().size());
        assertEquals("Petty Cash", currentAsset.getSubGroups().get(0).getLedgers().get(0).getLedgerName());
        assertEquals(0, BigDecimal.valueOf(25000).compareTo(currentAsset.getSubGroups().get(0).getLedgers().get(0).getOpeningDr()));
    }

    @Test
    void testWithNilBalance_filtersOutZeroBalances() {
        Ledger zeroLedger = createLedger(999L, "Dormant A/c", 1L, BigDecimal.ZERO, AccountEntryType.DR);
        when(ledgerRepository.findAll()).thenReturn(List.of(zeroLedger));
        when(voucherEntryRepository.sumNetByLedgerBeforeDate(any())).thenReturn(List.of());
        when(voucherEntryRepository.sumNetByLedgerBetweenDates(any(), any())).thenReturn(List.of());

        // withNilBalance = false
        TrialBalanceResponse withoutNil = trialBalanceService.getTrialBalance(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "SUMMARY",
                false
        );
        assertTrue(withoutNil.getGroups().isEmpty(), "Dormant zero account group should be filtered out");

        // withNilBalance = true
        TrialBalanceResponse withNil = trialBalanceService.getTrialBalance(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "SUMMARY",
                true
        );
        assertFalse(withNil.getGroups().isEmpty(), "Dormant account group should be included when withNilBalance is true");
    }

    @Test
    void testExcessOpeningAndClosingDifference() {
        // Only 100k Debit, no Credit -> Excess Dr
        Ledger cash = createLedger(102L, "Petty Cash", 29L, BigDecimal.valueOf(100000), AccountEntryType.DR);
        when(ledgerRepository.findAll()).thenReturn(List.of(cash));
        when(voucherEntryRepository.sumNetByLedgerBeforeDate(any())).thenReturn(List.of());
        when(voucherEntryRepository.sumNetByLedgerBetweenDates(any(), any())).thenReturn(List.of());

        TrialBalanceResponse response = trialBalanceService.getTrialBalance(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "SUMMARY",
                false
        );

        assertEquals("EXCESS_DR", response.getOpeningDifferenceType());
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getOpeningDifference()));
        assertEquals("EXCESS_DR", response.getClosingDifferenceType());
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getClosingDifference()));
    }

    private AccountGroup createGroup(Long id, String name, Long parentId, String nature, in.lekhai.common.AccountEntryType behaviour, boolean isPrimary) {
        AccountGroup group = new AccountGroup(name, parentId, nature, behaviour, isPrimary);
        group.setId(id);
        return group;
    }

    private Ledger createLedger(Long id, String name, Long groupId, BigDecimal opening, AccountEntryType type) {
        Ledger ledger = new Ledger(name, null, groupId, opening, type,
                null, null, null, null, null, null, null, null, null, null, null, null, null);
        ledger.setId(id);
        ledger.setActive(true);
        return ledger;
    }
}
