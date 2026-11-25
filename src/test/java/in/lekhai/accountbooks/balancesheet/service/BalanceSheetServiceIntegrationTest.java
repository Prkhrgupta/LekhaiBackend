package in.lekhai.accountbooks.balancesheet.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.BalanceSheetGroupItem;
import in.lekhai.contract.model.BalanceSheetResponse;
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
class BalanceSheetServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BalanceSheetService balanceSheetService;

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
    void balanceSheetCalculatesBalancesAndNetProfitCorrectly() {
        BalanceSheetResponse response = balanceSheetService.getBalanceSheet(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                false,
                "DETAIL"
        );

        assertNotNull(response);
        assertEquals("DETAIL", response.getFormat());
        assertTrue(response.getIsProfit());
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(response.getNetProfitLoss()));

        // Total Assets: Cash (5000 + 3000 = 8000) + Bank (7000 - 1000 = 6000) = 14000
        assertEquals(0, BigDecimal.valueOf(14000).compareTo(response.getTotalAssets()));

        // Total Liabilities: Capital (10000) + Creditor (2000) + Net Profit (2000) = 14000
        assertEquals(0, BigDecimal.valueOf(14000).compareTo(response.getTotalLiabilities()));

        // Balanced: difference is zero
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getDifferenceInBalance()));

        // Check Detail hierarchy
        boolean foundCapital = response.getLiabilities().stream()
                .anyMatch(g -> "Capital Account".equals(g.getGroupName()) && g.getAmount().compareTo(BigDecimal.valueOf(10000)) == 0);
        assertTrue(foundCapital, "Capital Account group must exist on Liabilities side");

        boolean foundCurrentAssets = response.getAssets().stream()
                .anyMatch(g -> "Current Assets".equals(g.getGroupName()) && g.getAmount().compareTo(BigDecimal.valueOf(14000)) == 0);
        assertTrue(foundCurrentAssets, "Current Assets group must exist on Assets side");
    }

    @Test
    void balanceSheetSummaryFormat() {
        BalanceSheetResponse response = balanceSheetService.getBalanceSheet(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                false,
                "SUMMARY"
        );

        assertNotNull(response);
        assertEquals("SUMMARY", response.getFormat());
        assertEquals(0, BigDecimal.valueOf(14000).compareTo(response.getTotalAssets()));
        assertEquals(0, BigDecimal.valueOf(14000).compareTo(response.getTotalLiabilities()));

        for (BalanceSheetGroupItem item : response.getLiabilities()) {
            assertTrue(item.getSubGroups().isEmpty());
            assertTrue(item.getLedgers().isEmpty());
        }
    }

    private void seed(Statement statement) throws Exception {
        statement.execute("""
                INSERT INTO account_group (id, name, nature, behaviour, is_primary, shop_code)
                VALUES (1, 'Capital Account', 'LIABILITY', 'CR', TRUE, 1),
                       (2, 'Current Liabilities', 'LIABILITY', 'CR', TRUE, 1),
                       (3, 'Current Assets', 'ASSET', 'DR', TRUE, 1),
                       (4, 'Direct Incomes', 'INCOME', 'CR', TRUE, 1),
                       (5, 'Direct Expenses', 'EXPENSE', 'DR', TRUE, 1)
                """);

        statement.execute("""
                INSERT INTO ledger (id, name, legal_name, account_group_id, opening_balance,
                                    opening_balance_type, is_active, shop_code)
                VALUES (1, 'Owner Capital', NULL, 1, 10000.00, 'CR', TRUE, 1),
                       (2, 'Vendor A', NULL, 2, 2000.00, 'CR', TRUE, 1),
                       (3, 'Cash', NULL, 3, 5000.00, 'DR', TRUE, 1),
                       (4, 'Bank', NULL, 3, 7000.00, 'DR', TRUE, 1),
                       (5, 'Sales A/c', NULL, 4, 0.00, 'CR', TRUE, 1),
                       (6, 'Rent Expense', NULL, 5, 0.00, 'DR', TRUE, 1)
                """);

        // Voucher 1: Sale of 3,000 for Cash (Cash DR 3000, Sales CR 3000)
        statement.execute("""
                INSERT INTO voucher (id, shop_code, voucher_type, voucher_number, voucher_date, narration)
                VALUES (1, 1, 'SALES', 1, '2026-05-10', 'Cash sale')
                """);
        statement.execute("""
                INSERT INTO voucher_entry (shop_code, voucher_id, ledger_id, line_number, debit_amount, credit_amount)
                VALUES (1, 1, 3, 1, 3000.00, 0.00),
                       (1, 1, 5, 2, 0.00, 3000.00)
                """);

        // Voucher 2: Rent payment of 1,000 via Bank (Rent DR 1000, Bank CR 1000)
        statement.execute("""
                INSERT INTO voucher (id, shop_code, voucher_type, voucher_number, voucher_date, narration)
                VALUES (2, 1, 'PAYMENT', 2, '2026-06-01', 'Rent payment')
                """);
        statement.execute("""
                INSERT INTO voucher_entry (shop_code, voucher_id, ledger_id, line_number, debit_amount, credit_amount)
                VALUES (1, 2, 6, 1, 1000.00, 0.00),
                       (1, 2, 4, 2, 0.00, 1000.00)
                """);
    }
}
