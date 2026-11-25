package in.lekhai.accountbooks.profitandloss.service;

import in.lekhai.contract.model.ProfitAndLossGroupItem;
import in.lekhai.contract.model.ProfitAndLossResponse;
import in.lekhai.shop.context.model.ShopContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true"
})
class ProfitAndLossServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProfitAndLossService profitAndLossService;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        ShopContext.setShopCode(1);
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET app.shop_code = '1'");
                statement.execute("TRUNCATE voucher_entry, voucher, ledger, account_group RESTART IDENTITY CASCADE");
                seed(statement);
            }
        }
    }

    @AfterEach
    void tearDown() {
        ShopContext.clear();
    }

    @Test
    void profitAndLossCalculatesTradingAndNetProfitCorrectly() {
        ProfitAndLossResponse response = profitAndLossService.getProfitAndLoss(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "MANUAL",
                BigDecimal.valueOf(5000),  // Opening stock: 5,000
                BigDecimal.valueOf(10000), // Closing stock: 10,000
                "DETAIL"
        );

        assertNotNull(response);
        assertEquals("DETAIL", response.getFormat());
        assertEquals("MANUAL", response.getStockMethod());

        // Direct Incomes: Sales A/c = 50,000
        // Closing stock = 10,000 -> Total Trading Credit = 60,000
        // Opening stock = 5,000 + Purchases (Direct Expense) = 20,000 -> Total Trading Debit = 25,000
        // Gross Profit = 60,000 - 25,000 = 35,000
        assertTrue(response.getIsGrossProfit());
        assertEquals(0, BigDecimal.valueOf(35000).compareTo(response.getGrossProfit()));

        // P&L: GP (35,000) - Rent Expense (Indirect Expense: 5,000) = Net Profit 30,000
        assertTrue(response.getIsNetProfit());
        assertEquals(0, BigDecimal.valueOf(30000).compareTo(response.getNetProfit()));

        // Verify Direct Incomes detail has Sales A/c
        boolean foundSales = response.getIncome().stream()
                .filter(g -> g.getGroupId() == 11L)
                .flatMap(g -> g.getLedgers().stream())
                .anyMatch(l -> "Domestic Sales".equals(l.getLedgerName()) && l.getAmount().compareTo(BigDecimal.valueOf(50000)) == 0);
        assertTrue(foundSales, "Domestic Sales ledger must exist in Direct Incomes");
    }

    @Test
    void profitAndLossSummaryFormat() {
        ProfitAndLossResponse response = profitAndLossService.getProfitAndLoss(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "MANUAL",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "SUMMARY"
        );

        assertNotNull(response);
        assertEquals("SUMMARY", response.getFormat());

        for (ProfitAndLossGroupItem item : response.getExpenses()) {
            assertTrue(item.getSubGroups().isEmpty());
            assertTrue(item.getLedgers().isEmpty());
        }
        for (ProfitAndLossGroupItem item : response.getIncome()) {
            assertTrue(item.getSubGroups().isEmpty());
            assertTrue(item.getLedgers().isEmpty());
        }
    }

    private void seed(Statement statement) throws Exception {
        statement.execute("""
                INSERT INTO account_group (id, name, nature, behaviour, is_primary, shop_code)
                VALUES (1, 'Capital Account', 'LIABILITY', 'CR', TRUE, 1),
                       (6, 'Current Assets', 'ASSET', 'DR', TRUE, 1),
                       (11, 'Direct Incomes', 'INCOME', 'CR', TRUE, 1),
                       (12, 'Indirect Incomes', 'INCOME', 'CR', TRUE, 1),
                       (13, 'Direct Expenses', 'EXPENSE', 'DR', TRUE, 1),
                       (14, 'Indirect Expenses', 'EXPENSE', 'DR', TRUE, 1)
                """);

        statement.execute("""
                INSERT INTO ledger (id, name, legal_name, account_group_id, opening_balance,
                                    opening_balance_type, is_active, shop_code)
                VALUES (1, 'Domestic Sales', NULL, 11, 0.00, 'CR', TRUE, 1),
                       (2, 'Purchases', NULL, 13, 0.00, 'DR', TRUE, 1),
                       (3, 'Office Rent', NULL, 14, 0.00, 'DR', TRUE, 1),
                       (4, 'Cash', NULL, 6, 100000.00, 'DR', TRUE, 1)
                """);

        // Voucher 1: Sale of 50,000 (Cash DR 50000, Sales CR 50000)
        statement.execute("""
                INSERT INTO voucher (id, shop_code, voucher_type, voucher_number, voucher_date, narration)
                VALUES (1, 1, 'SALES', 1, '2026-05-10', 'Sales')
                """);
        statement.execute("""
                INSERT INTO voucher_entry (shop_code, voucher_id, ledger_id, line_number, debit_amount, credit_amount)
                VALUES (1, 1, 4, 1, 50000.00, 0.00),
                       (1, 1, 1, 2, 0.00, 50000.00)
                """);

        // Voucher 2: Purchases of 20,000 (Purchases DR 20000, Cash CR 20000)
        statement.execute("""
                INSERT INTO voucher (id, shop_code, voucher_type, voucher_number, voucher_date, narration)
                VALUES (2, 1, 'PAYMENT', 2, '2026-05-15', 'Purchases')
                """);
        statement.execute("""
                INSERT INTO voucher_entry (shop_code, voucher_id, ledger_id, line_number, debit_amount, credit_amount)
                VALUES (1, 2, 2, 1, 20000.00, 0.00),
                       (1, 2, 4, 2, 0.00, 20000.00)
                """);

        // Voucher 3: Rent payment of 5,000 (Office Rent DR 5000, Cash CR 5000)
        statement.execute("""
                INSERT INTO voucher (id, shop_code, voucher_type, voucher_number, voucher_date, narration)
                VALUES (3, 1, 'PAYMENT', 3, '2026-06-01', 'Rent payment')
                """);
        statement.execute("""
                INSERT INTO voucher_entry (shop_code, voucher_id, ledger_id, line_number, debit_amount, credit_amount)
                VALUES (1, 3, 3, 1, 5000.00, 0.00),
                       (1, 3, 4, 2, 0.00, 5000.00)
                """);
    }
}
