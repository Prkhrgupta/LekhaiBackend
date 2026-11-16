package in.lekhai.accountbooks.trialbalance.service;

import in.lekhai.contract.model.TrialBalanceResponse;
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
class TrialBalanceServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TrialBalanceService trialBalanceService;

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
    void trialBalanceCalculatesMovementsAndBalancesCorrectly() {
        TrialBalanceResponse response = trialBalanceService.getTrialBalance(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "DETAIL",
                false
        );

        assertNotNull(response);
        assertEquals("DETAIL", response.getFormat());

        // Opening: Capital 100k Cr, Cash 100k Dr -> Balanced at 100k
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getTotalOpeningDr()));
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(response.getTotalOpeningCr()));
        assertEquals("BALANCED", response.getOpeningDifferenceType());

        // Transactions: Sales 50k Cr, Cash 50k Dr
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(response.getTotalTransactionsDr()));
        assertEquals(0, BigDecimal.valueOf(50000).compareTo(response.getTotalTransactionsCr()));

        // Closing: Cash 150k Dr, Capital 100k Cr, Sales 50k Cr -> Balanced at 150k
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(response.getTotalClosingDr()));
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(response.getTotalClosingCr()));
        assertEquals("BALANCED", response.getClosingDifferenceType());
    }

    @Test
    void trialBalanceSummaryFormat() {
        TrialBalanceResponse response = trialBalanceService.getTrialBalance(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "SUMMARY",
                false
        );

        assertNotNull(response);
        assertEquals("SUMMARY", response.getFormat());
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(response.getTotalClosingDr()));
        assertEquals(0, BigDecimal.valueOf(150000).compareTo(response.getTotalClosingCr()));
    }

    private void seed(Statement statement) throws Exception {
        statement.execute("""
                INSERT INTO account_group (id, name, nature, behaviour, is_primary, shop_code)
                VALUES (1, 'Capital Account', 'LIABILITY', 'CR', TRUE, 1),
                       (6, 'Current Assets', 'ASSET', 'DR', TRUE, 1),
                       (11, 'Direct Incomes', 'INCOME', 'CR', TRUE, 1)
                """);

        statement.execute("""
                INSERT INTO ledger (id, name, legal_name, account_group_id, opening_balance,
                                    opening_balance_type, is_active, shop_code)
                VALUES (1, 'Owner Capital', NULL, 1, 100000.00, 'CR', TRUE, 1),
                       (2, 'Cash in Hand', NULL, 6, 100000.00, 'DR', TRUE, 1),
                       (3, 'Domestic Sales', NULL, 11, 0.00, 'CR', TRUE, 1)
                """);

        statement.execute("""
                INSERT INTO voucher (id, shop_code, voucher_type, voucher_number, voucher_date, narration)
                VALUES (1, 1, 'SALES', 1, '2026-05-10', 'Sales')
                """);
        statement.execute("""
                INSERT INTO voucher_entry (shop_code, voucher_id, ledger_id, line_number, debit_amount, credit_amount)
                VALUES (1, 1, 2, 1, 50000.00, 0.00),
                       (1, 1, 3, 2, 0.00, 50000.00)
                """);
    }
}
