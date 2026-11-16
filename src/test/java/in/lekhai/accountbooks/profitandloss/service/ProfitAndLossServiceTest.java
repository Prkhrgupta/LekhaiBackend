package in.lekhai.accountbooks.profitandloss.service;

import in.lekhai.accountbooks.balancesheet.repository.LedgerNetTotalProjection;
import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.ProfitAndLossGroupItem;
import in.lekhai.contract.model.ProfitAndLossResponse;
import in.lekhai.core.account_master.domain.AccountGroup;
import in.lekhai.core.account_master.domain.Ledger;
import in.lekhai.core.account_master.repository.AccountGroupRepository;
import in.lekhai.core.account_master.repository.LedgerRepository;
import in.lekhai.salesinvoice.repository.StockLedgerRepository;
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
class ProfitAndLossServiceTest {

    @Mock
    private AccountGroupRepository accountGroupRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private VoucherEntryRepository voucherEntryRepository;

    @Mock
    private StockLedgerRepository stockLedgerRepository;

    @InjectMocks
    private ProfitAndLossService profitAndLossService;

    private AccountGroup directIncomes;
    private AccountGroup indirectIncomes;
    private AccountGroup directExpenses;
    private AccountGroup indirectExpenses;
    private AccountGroup salesAccountSubGroup;

    @BeforeEach
    void setUp() {
        directIncomes = createGroup(11L, "Direct Incomes", null, "INCOME", in.lekhai.common.AccountEntryType.CR, true);
        indirectIncomes = createGroup(12L, "Indirect Incomes", null, "INCOME", in.lekhai.common.AccountEntryType.CR, true);
        directExpenses = createGroup(13L, "Direct Expenses", null, "EXPENSE", in.lekhai.common.AccountEntryType.DR, true);
        indirectExpenses = createGroup(14L, "Indirect Expenses", null, "EXPENSE", in.lekhai.common.AccountEntryType.DR, true);
        salesAccountSubGroup = createGroup(36L, "Sales Account", 11L, "INCOME", in.lekhai.common.AccountEntryType.CR, false);

        lenient().when(accountGroupRepository.findAll()).thenReturn(List.of(
                directIncomes, indirectIncomes, directExpenses, indirectExpenses, salesAccountSubGroup
        ));
    }

    @Test
    void testManualStock_GrossProfitAndNetProfit() {
        Ledger sales = createLedger(201L, "Domestic Sales", 36L, BigDecimal.ZERO, AccountEntryType.CR);
        Ledger purchase = createLedger(202L, "Purchase A/c", 13L, BigDecimal.ZERO, AccountEntryType.DR);
        Ledger rent = createLedger(203L, "Office Rent", 14L, BigDecimal.ZERO, AccountEntryType.DR);
        Ledger discount = createLedger(204L, "Discount Received", 12L, BigDecimal.ZERO, AccountEntryType.CR);

        when(ledgerRepository.findAll()).thenReturn(List.of(sales, purchase, rent, discount));
        when(voucherEntryRepository.sumNetByLedgerBetweenDates(any(), any())).thenReturn(List.of(
                new LedgerNetTotalProjection(201L, BigDecimal.ZERO, BigDecimal.valueOf(100000)), // Sales 100k Cr
                new LedgerNetTotalProjection(202L, BigDecimal.valueOf(40000), BigDecimal.ZERO),  // Purchase 40k Dr
                new LedgerNetTotalProjection(203L, BigDecimal.valueOf(15000), BigDecimal.ZERO),  // Rent 15k Dr
                new LedgerNetTotalProjection(204L, BigDecimal.ZERO, BigDecimal.valueOf(5000))    // Discount 5k Cr
        ));

        ProfitAndLossResponse response = profitAndLossService.getProfitAndLoss(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "MANUAL",
                BigDecimal.valueOf(10000), // Opening stock
                BigDecimal.valueOf(20000), // Closing stock
                "SUMMARY"
        );

        assertNotNull(response);
        assertEquals("MANUAL", response.getStockMethod());
        assertEquals(0, BigDecimal.valueOf(10000).compareTo(response.getOpeningStock()));
        assertEquals(0, BigDecimal.valueOf(20000).compareTo(response.getClosingStock()));

        // Trading: Credit (100k sales + 20k closing = 120k) - Debit (10k opening + 40k purchase = 50k) = 70k Gross Profit
        assertTrue(response.getIsGrossProfit());
        assertEquals(0, BigDecimal.valueOf(70000).compareTo(response.getGrossProfit()));

        // P&L: Credit (70k GP + 5k discount = 75k) - Debit (15k rent = 15k) = 60k Net Profit
        assertTrue(response.getIsNetProfit());
        assertEquals(0, BigDecimal.valueOf(60000).compareTo(response.getNetProfit()));

        // Verify summary mode groups have no nested detail items
        for (ProfitAndLossGroupItem item : response.getExpenses()) {
            assertTrue(item.getSubGroups().isEmpty());
            assertTrue(item.getLedgers().isEmpty());
        }
    }

    @Test
    void testDetailFormat_includesHierarchy() {
        Ledger sales = createLedger(201L, "Domestic Sales", 36L, BigDecimal.ZERO, AccountEntryType.CR);
        when(ledgerRepository.findAll()).thenReturn(List.of(sales));
        when(voucherEntryRepository.sumNetByLedgerBetweenDates(any(), any())).thenReturn(List.of(
                new LedgerNetTotalProjection(201L, BigDecimal.ZERO, BigDecimal.valueOf(50000))
        ));

        ProfitAndLossResponse response = profitAndLossService.getProfitAndLoss(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "MANUAL",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "DETAIL"
        );

        assertNotNull(response);
        assertEquals("DETAIL", response.getFormat());

        // Find Direct Incomes group in income list
        ProfitAndLossGroupItem directIncomeGroup = response.getIncome().stream()
                .filter(g -> g.getGroupId().equals(11L))
                .findFirst()
                .orElseThrow();

        assertEquals("Direct Incomes", directIncomeGroup.getGroupName());
        assertEquals(1, directIncomeGroup.getSubGroups().size());
        assertEquals("Sales Account", directIncomeGroup.getSubGroups().get(0).getGroupName());
        assertEquals(1, directIncomeGroup.getSubGroups().get(0).getLedgers().size());
        assertEquals("Domestic Sales", directIncomeGroup.getSubGroups().get(0).getLedgers().get(0).getLedgerName());
    }

    @Test
    void testAutoStock_fromStockLedger() {
        when(ledgerRepository.findAll()).thenReturn(List.of());
        when(voucherEntryRepository.sumNetByLedgerBetweenDates(any(), any())).thenReturn(List.of());
        when(stockLedgerRepository.sumNetValueBeforeDate(any())).thenReturn(BigDecimal.valueOf(25000));
        when(stockLedgerRepository.sumNetValueUpToDate(any())).thenReturn(BigDecimal.valueOf(45000));

        ProfitAndLossResponse response = profitAndLossService.getProfitAndLoss(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "AUTO",
                null,
                null,
                "SUMMARY"
        );

        assertNotNull(response);
        assertEquals("AUTO", response.getStockMethod());
        assertEquals(0, BigDecimal.valueOf(25000).compareTo(response.getOpeningStock()));
        assertEquals(0, BigDecimal.valueOf(45000).compareTo(response.getClosingStock()));
        // Trading: 45000 closing - 25000 opening = 20000 GP
        assertTrue(response.getIsGrossProfit());
        assertEquals(0, BigDecimal.valueOf(20000).compareTo(response.getGrossProfit()));
    }

    @Test
    void testGrossLossAndNetLoss() {
        Ledger purchase = createLedger(202L, "Purchase A/c", 13L, BigDecimal.ZERO, AccountEntryType.DR);
        when(ledgerRepository.findAll()).thenReturn(List.of(purchase));
        when(voucherEntryRepository.sumNetByLedgerBetweenDates(any(), any())).thenReturn(List.of(
                new LedgerNetTotalProjection(202L, BigDecimal.valueOf(50000), BigDecimal.ZERO) // 50k Purchase Dr, 0 Sales
        ));

        ProfitAndLossResponse response = profitAndLossService.getProfitAndLoss(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "MANUAL",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "SUMMARY"
        );

        assertNotNull(response);
        assertFalse(response.getIsGrossProfit());
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(response.getGrossProfit())); // Gross Loss: 50,000

        assertFalse(response.getIsNetProfit());
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(response.getNetProfit())); // Net Loss: 50,000
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
