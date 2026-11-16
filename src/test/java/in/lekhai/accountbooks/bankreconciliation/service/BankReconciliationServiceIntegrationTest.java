package in.lekhai.accountbooks.bankreconciliation.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.BankReconciliationMode;
import in.lekhai.contract.model.BankReconciliationResponse;
import in.lekhai.contract.model.UpdateClearDateResponse;
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
class BankReconciliationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BankReconciliationService bankReconciliationService;

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
                (30, 'Bank Accounts', 6, 'ASSET', 'DR', false, 1),
                (3, 'Sundry Debtors', 6, 'ASSET', 'DR', false, 1),
                (4, 'Sundry Creditors', NULL, 'LIABILITY', 'CR', true, 1);
        """);

        statement.execute("""
            INSERT INTO ledger (id, name, account_group_id, opening_balance, opening_balance_type, is_active, shop_code)
            VALUES
                (19, 'HDFC 0343', 30, 100000.00, 'DR', true, 1),
                (22, 'AJAY AUTO PARTS', 3, 0.00, 'DR', true, 1),
                (13, 'BHARAT TEXTILES', 4, 0.00, 'CR', true, 1);
        """);

        statement.execute("""
            INSERT INTO voucher (id, voucher_type, voucher_number, voucher_date, narration, shop_code)
            VALUES
                (101, 'RECEIPT', 1, '2026-05-15', 'Received via NEFT from customer', 1),
                (102, 'PAYMENT', 1, '2026-05-20', 'Paid to vendor via UPI', 1);
        """);

        statement.execute("""
            INSERT INTO voucher_entry (id, voucher_id, ledger_id, line_number, debit_amount, credit_amount, clear_date, remarks, shop_code)
            VALUES
                (1, 101, 19, 1, 40000.00, 0.00, '2026-05-16', '', 1),
                (2, 101, 22, 2, 0.00, 40000.00, NULL, '', 1),
                (3, 102, 13, 1, 25000.00, 0.00, NULL, '', 1),
                (4, 102, 19, 2, 0.00, 25000.00, NULL, '', 1);
        """);
    }

    @Test
    void testBankReconciliation_AllModesAndClearDateUpdate() {
        LocalDate fromDate = LocalDate.of(2026, 4, 1);
        LocalDate toDate = LocalDate.of(2027, 3, 31);

        // 1. ALL mode
        BankReconciliationResponse allResponse = bankReconciliationService.getBankReconciliation(
                19L, fromDate, toDate, BankReconciliationMode.ALL
        );
        assertNotNull(allResponse);
        assertEquals("HDFC 0343", allResponse.getBankName());
        assertEquals("Bank Accounts Under : Current Assets", allResponse.getAccountGroup());
        assertEquals(2, allResponse.getEntries().size());

        assertEquals(0, new BigDecimal("115000").compareTo(allResponse.getClosingBalance()));
        assertEquals(AccountEntryType.DR, allResponse.getClosingBalanceType());

        // Entry 1 is cleared
        assertTrue(allResponse.getEntries().get(0).getIsCleared());
        assertEquals("NEFT", allResponse.getEntries().get(0).getCheque());
        assertEquals(0, new BigDecimal("40000").compareTo(allResponse.getEntries().get(0).getDeposits().get()));
        assertEquals(LocalDate.of(2026, 5, 16), allResponse.getEntries().get(0).getClearDate().get());

        // Entry 2 (voucher_entry 4) is uncleared
        assertFalse(allResponse.getEntries().get(1).getIsCleared());
        assertEquals(0, new BigDecimal("25000").compareTo(allResponse.getEntries().get(1).getWithdrawals().get()));

        // 2. UNCLEARED mode
        BankReconciliationResponse unclearedResponse = bankReconciliationService.getBankReconciliation(
                19L, fromDate, toDate, BankReconciliationMode.UNCLEARED
        );
        assertEquals(1, unclearedResponse.getEntries().size());
        assertEquals(4L, unclearedResponse.getEntries().get(0).getVoucherEntryId());

        // 3. CLEARED mode
        BankReconciliationResponse clearedResponse = bankReconciliationService.getBankReconciliation(
                19L, fromDate, toDate, BankReconciliationMode.CLEARED
        );
        assertEquals(1, clearedResponse.getEntries().size());
        assertEquals(1L, clearedResponse.getEntries().get(0).getVoucherEntryId());

        // 4. Update clear date on entry 4
        UpdateClearDateResponse updateResponse = bankReconciliationService.updateClearDate(
                4L, LocalDate.of(2026, 5, 22)
        );
        assertTrue(updateResponse.getSuccess());

        // 5. Verify uncleared is now empty
        BankReconciliationResponse unclearedAfter = bankReconciliationService.getBankReconciliation(
                19L, fromDate, toDate, BankReconciliationMode.UNCLEARED
        );
        assertEquals(0, unclearedAfter.getEntries().size());
    }
}
