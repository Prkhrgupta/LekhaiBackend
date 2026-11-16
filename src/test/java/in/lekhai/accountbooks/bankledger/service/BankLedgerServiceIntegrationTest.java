package in.lekhai.accountbooks.bankledger.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.BankLedgerResponse;
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
class BankLedgerServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private BankLedgerService bankLedgerService;

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
            INSERT INTO voucher_entry (id, voucher_id, ledger_id, line_number, debit_amount, credit_amount, remarks, shop_code)
            VALUES
                (1, 101, 19, 1, 40000.00, 0.00, '', 1),
                (2, 101, 22, 2, 0.00, 40000.00, '', 1),
                (3, 102, 13, 1, 25000.00, 0.00, '', 1),
                (4, 102, 19, 2, 0.00, 25000.00, '', 1);
        """);
    }

    @Test
    void testGetBankLedger_IntegrationFlow() {
        BankLedgerResponse response = bankLedgerService.getBankLedger(
                19L,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2027, 3, 31)
        );

        assertNotNull(response);
        assertEquals("HDFC 0343", response.getBankName());
        assertEquals("Bank Accounts Under : Current Assets", response.getAccountGroup());
        assertEquals(0, new BigDecimal("100000").compareTo(response.getOpeningBalance()));
        assertEquals(AccountEntryType.DR, response.getOpeningBalanceType());
        assertEquals(0, new BigDecimal("40000").compareTo(response.getTotalDebit()));
        assertEquals(0, new BigDecimal("25000").compareTo(response.getTotalCredit()));
        assertEquals(0, new BigDecimal("115000").compareTo(response.getClosingBalance()));
        assertEquals(AccountEntryType.DR, response.getClosingBalanceType());

        assertEquals(3, response.getEntries().size());
        assertEquals("OPENING", response.getEntries().get(0).getEntryType());

        assertEquals("To AJAY AUTO PARTS", response.getEntries().get(1).getParticulars());
        assertEquals("Rcpt", response.getEntries().get(1).getVchType());
        assertEquals("NEFT", response.getEntries().get(1).getInstrumentMode());
        assertEquals(0, new BigDecimal("40000").compareTo(response.getEntries().get(1).getDebit()));

        assertEquals("By BHARAT TEXTILES", response.getEntries().get(2).getParticulars());
        assertEquals("Pymt", response.getEntries().get(2).getVchType());
        assertEquals("UPI", response.getEntries().get(2).getInstrumentMode());
        assertEquals(0, new BigDecimal("25000").compareTo(response.getEntries().get(2).getCredit()));
    }
}
