package in.lekhai.accountbooks.balancesheet.service;

import in.lekhai.accountbooks.balancesheet.repository.LedgerNetTotalProjection;
import in.lekhai.accountbooks.balancesheet.repository.NatureTotalProjection;
import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.BalanceSheetGroupItem;
import in.lekhai.contract.model.BalanceSheetResponse;
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
class BalanceSheetServiceTest {

    @Mock
    private AccountGroupRepository accountGroupRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private VoucherEntryRepository voucherEntryRepository;

    @InjectMocks
    private BalanceSheetService balanceSheetService;

    private AccountGroup capitalGroup;
    private AccountGroup currentLiabGroup;
    private AccountGroup sundryCreditorsGroup;
    private AccountGroup currentAssetGroup;
    private AccountGroup bankAccountsGroup;

    @BeforeEach
    void setUp() {
        capitalGroup = createGroup(1L, "Capital Account", null, "LIABILITY", in.lekhai.common.AccountEntryType.CR, true);
        currentLiabGroup = createGroup(3L, "Current Liabilities", null, "LIABILITY", in.lekhai.common.AccountEntryType.CR, true);
        sundryCreditorsGroup = createGroup(21L, "Sundry Creditors", 3L, "LIABILITY", in.lekhai.common.AccountEntryType.CR, false);
        currentAssetGroup = createGroup(6L, "Current Assets", null, "ASSET", in.lekhai.common.AccountEntryType.DR, true);
        bankAccountsGroup = createGroup(30L, "Bank Accounts", 6L, "ASSET", in.lekhai.common.AccountEntryType.DR, false);

        lenient().when(accountGroupRepository.findAll()).thenReturn(List.of(
                capitalGroup, currentLiabGroup, sundryCreditorsGroup, currentAssetGroup, bankAccountsGroup
        ));
    }

    @Test
    void testSummaryFormat_withNetProfit() {
        Ledger capital = createLedger(101L, "Owner Capital", 1L, BigDecimal.valueOf(100000), AccountEntryType.CR);
        Ledger creditor = createLedger(102L, "Vendor Alpha", 21L, BigDecimal.valueOf(20000), AccountEntryType.CR);
        Ledger bank = createLedger(103L, "HDFC Bank", 30L, BigDecimal.valueOf(150000), AccountEntryType.DR);

        when(ledgerRepository.findAll()).thenReturn(List.of(capital, creditor, bank));
        when(voucherEntryRepository.sumNetByLedgerUpToDate(any())).thenReturn(List.of(
                new LedgerNetTotalProjection(101L, BigDecimal.ZERO, BigDecimal.ZERO),
                new LedgerNetTotalProjection(102L, BigDecimal.ZERO, BigDecimal.ZERO),
                new LedgerNetTotalProjection(103L, BigDecimal.ZERO, BigDecimal.ZERO)
        ));

        // Income: 50,000 credit, 0 debit; Expense: 20,000 debit, 0 credit => Net profit = 30,000
        when(voucherEntryRepository.sumIncomeExpenseBetweenDates(any(), any())).thenReturn(List.of(
                new NatureTotalProjection("INCOME", BigDecimal.ZERO, BigDecimal.valueOf(50000)),
                new NatureTotalProjection("EXPENSE", BigDecimal.valueOf(20000), BigDecimal.ZERO)
        ));

        BalanceSheetResponse response = balanceSheetService.getBalanceSheet(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                false,
                "SUMMARY"
        );

        assertNotNull(response);
        assertEquals("SUMMARY", response.getFormat());
        assertTrue(response.getIsProfit());
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(response.getNetProfitLoss()));

        // Liabilities: Capital (100k) + Current Liabilities (20k) + P&L (30k) = 150k
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(response.getTotalLiabilities()));

        // Assets: Current Assets (150k) = 150k
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(response.getTotalAssets()));

        // Difference: 0 (balanced)
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getDifferenceInBalance()));

        // Verify summary items do not contain nested detail lists
        for (BalanceSheetGroupItem g : response.getLiabilities()) {
            assertTrue(g.getSubGroups().isEmpty());
            assertTrue(g.getLedgers().isEmpty());
        }
    }

    @Test
    void testDetailFormat_includesHierarchy() {
        Ledger creditor = createLedger(102L, "Vendor Alpha", 21L, BigDecimal.valueOf(20000), AccountEntryType.CR);
        Ledger bank = createLedger(103L, "HDFC Bank", 30L, BigDecimal.valueOf(20000), AccountEntryType.DR);

        when(ledgerRepository.findAll()).thenReturn(List.of(creditor, bank));
        when(voucherEntryRepository.sumNetByLedgerUpToDate(any())).thenReturn(List.of(
                new LedgerNetTotalProjection(102L, BigDecimal.ZERO, BigDecimal.ZERO),
                new LedgerNetTotalProjection(103L, BigDecimal.ZERO, BigDecimal.ZERO)
        ));

        BalanceSheetResponse response = balanceSheetService.getBalanceSheet(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                true,
                "DETAIL"
        );

        assertNotNull(response);
        assertEquals("DETAIL", response.getFormat());

        // Check Liabilities detail
        assertEquals(1, response.getLiabilities().size());
        BalanceSheetGroupItem currentLiab = response.getLiabilities().get(0);
        assertEquals("Current Liabilities", currentLiab.getGroupName());
        assertEquals(1, currentLiab.getSubGroups().size());
        assertEquals("Sundry Creditors", currentLiab.getSubGroups().get(0).getGroupName());
        assertEquals(1, currentLiab.getSubGroups().get(0).getLedgers().size());
        assertEquals("Vendor Alpha", currentLiab.getSubGroups().get(0).getLedgers().get(0).getLedgerName());

        // Check Assets detail
        assertEquals(1, response.getAssets().size());
        BalanceSheetGroupItem currentAsset = response.getAssets().get(0);
        assertEquals("Current Assets", currentAsset.getGroupName());
        assertEquals(1, currentAsset.getSubGroups().size());
        assertEquals("Bank Accounts", currentAsset.getSubGroups().get(0).getGroupName());
        assertEquals(1, currentAsset.getSubGroups().get(0).getLedgers().size());
        assertEquals("HDFC Bank", currentAsset.getSubGroups().get(0).getLedgers().get(0).getLedgerName());
    }

    @Test
    void testBooksClosed_doesNotInjectUnclosedProfitLoss() {
        Ledger capital = createLedger(101L, "Owner Capital", 1L, BigDecimal.valueOf(100000), AccountEntryType.CR);
        Ledger bank = createLedger(103L, "HDFC Bank", 30L, BigDecimal.valueOf(100000), AccountEntryType.DR);

        when(ledgerRepository.findAll()).thenReturn(List.of(capital, bank));
        when(voucherEntryRepository.sumNetByLedgerUpToDate(any())).thenReturn(List.of());

        BalanceSheetResponse response = balanceSheetService.getBalanceSheet(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                true, // booksClosed = true
                "SUMMARY"
        );

        assertTrue(response.getBooksClosed());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getNetProfitLoss()));
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getTotalLiabilities()));
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getTotalAssets()));
        assertEquals(1, response.getLiabilities().size()); // only Capital, no P&L injected
    }

    @Test
    void testInvalidDateRange_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> balanceSheetService.getBalanceSheet(
                LocalDate.of(2027, 5, 1),
                LocalDate.of(2026, 5, 1),
                false,
                "SUMMARY"
        ));
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
