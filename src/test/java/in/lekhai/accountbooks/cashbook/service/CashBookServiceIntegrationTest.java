package in.lekhai.accountbooks.cashbook.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.CashBookResponse;
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
class CashBookServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CashBookService cashBookService;

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

    private void seed(Statement statement) throws Exception {
        statement.execute("""
            INSERT INTO account_group (id, name, parent_id, nature, behaviour, is_primary, shop_code)
            VALUES
                (6, 'Current Assets', NULL, 'ASSET', 'DR', true, 1),
                (29, 'Cash in Hand', 6, 'ASSET', 'DR', false, 1),
                (11, 'Direct Incomes', NULL, 'INCOME', 'CR', true, 1),
                (13, 'Direct Expenses', NULL, 'EXPENSE', 'DR', true, 1);
        """);

        statement.execute("""
            INSERT INTO ledger (id, name, account_group_id, opening_balance, opening_balance_type, is_active, shop_code)
            VALUES
                (21, 'Main Cash A/c', 29, 20000.00, 'DR', true, 1),
                (26, 'Domestic Sales A/c', 11, 0.00, 'CR', true, 1),
                (27, 'Factory Rent', 13, 0.00, 'DR', true, 1);
        """);

        statement.execute("""
            INSERT INTO voucher (id, voucher_type, voucher_number, voucher_date, narration, shop_code)
            VALUES
                (101, 'SALES', 1, '2026-05-10', 'Cash Sale 1', 1),
                (102, 'SALES', 2, '2026-05-10', 'Cash Sale 2', 1),
                (103, 'PAYMENT', 1, '2026-05-20', 'Rent paid', 1);
        """);

        statement.execute("""
            INSERT INTO voucher_entry (id, voucher_id, ledger_id, line_number, debit_amount, credit_amount, remarks, shop_code)
            VALUES
                (1, 101, 21, 1, 5000.00, 0.00, '', 1),
                (2, 101, 26, 2, 0.00, 5000.00, '', 1),
                (3, 102, 21, 1, 3000.00, 0.00, '', 1),
                (4, 102, 26, 2, 0.00, 3000.00, '', 1),
                (5, 103, 27, 1, 4000.00, 0.00, '', 1),
                (6, 103, 21, 2, 0.00, 4000.00, '', 1);
        """);
    }

    @Test
    void testCashBook_IntegrationFlow() {
        CashBookResponse response = cashBookService.getCashBook(
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31),
                "DETAIL",
                true,
                false,
                "DAILY",
                null
        );

        assertNotNull(response);
        assertEquals(0, new BigDecimal("20000").compareTo(response.getOpeningBalance()));
        assertEquals(AccountEntryType.DR, response.getOpeningBalanceType());
        assertEquals(0, new BigDecimal("8000").compareTo(response.getTotalReceived()));
        assertEquals(0, new BigDecimal("4000").compareTo(response.getTotalPayment()));
        assertEquals(0, new BigDecimal("24000").compareTo(response.getClosingBalance()));
        assertEquals(AccountEntryType.DR, response.getClosingBalanceType());
    }
}
