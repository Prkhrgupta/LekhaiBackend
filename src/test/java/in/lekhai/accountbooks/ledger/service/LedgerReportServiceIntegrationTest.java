package in.lekhai.accountbooks.ledger.service;

import in.lekhai.contract.model.AccountEntryType;
import in.lekhai.contract.model.AccountLedgerEntryItem;
import in.lekhai.contract.model.AccountLedgerPageResponse;
import in.lekhai.shop.context.model.ShopContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true"
})
class LedgerReportServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private LedgerReportService ledgerReportService;

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
    void dateRangeIsRespectedAndRunningBalanceIsCorrect() {
        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), PageRequest.of(0, 10));

        assertEquals(3, response.getData().size(), "opening balance + 2 entries expected");

        AccountLedgerEntryItem opening = response.getData().get(0);
        assertEquals("Opening Balance", opening.getLedgerName());
        assertEquals(AccountEntryType.DR, opening.getCrdr());
        assertEquals(0, opening.getBalance().compareTo(BigDecimal.valueOf(1300)));

        AccountLedgerEntryItem first = response.getData().get(1);
        assertEquals("2026-05-05", first.getDate());
        assertEquals("Cash", first.getLedgerName(), "contra ledger for V3 should be Cash");
        assertEquals(0, first.getDebitAmt().compareTo(BigDecimal.valueOf(300)));
        assertEquals(0, first.getBalance().compareTo(BigDecimal.valueOf(1600)));
        assertEquals(AccountEntryType.DR, first.getCrdr());

        AccountLedgerEntryItem second = response.getData().get(2);
        assertEquals("2026-05-06", second.getDate());
        assertEquals(0, second.getCreditAmt().compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, second.getBalance().compareTo(BigDecimal.valueOf(1500)));
        assertEquals(AccountEntryType.DR, second.getCrdr());

        assertEquals(2L, response.getPagination().getTotalElements());
        assertEquals(1, response.getPagination().getTotalPages());
    }

    @Test
    void pageBoundaryKeepsRunningBalanceContinuousForSameDateVouchers() {
        AccountLedgerPageResponse page0 = ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), PageRequest.of(0, 2));

        assertEquals(3, page0.getData().size(), "page 0 carries opening balance + 2 entries");
        assertEquals("Opening Balance", page0.getData().get(0).getLedgerName());
        assertEquals(0, page0.getData().get(0).getBalance().compareTo(BigDecimal.valueOf(1500)));
        assertEquals(0, page0.getData().get(1).getBalance().compareTo(BigDecimal.valueOf(1550)));
        assertEquals(0, page0.getData().get(2).getBalance().compareTo(BigDecimal.valueOf(1490)));
        assertEquals(3L, page0.getPagination().getTotalElements());
        assertEquals(2, page0.getPagination().getTotalPages());

        AccountLedgerPageResponse page1 = ledgerReportService.getAccountLedgerEntries(
                1L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 2), PageRequest.of(1, 2));

        assertEquals(1, page1.getData().size());
        assertEquals(0, page1.getData().get(0).getBalance().compareTo(BigDecimal.valueOf(1560)));
    }

    @Test
    void emptyLedgerReturnsEmptyPage() {
        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                4L, LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), PageRequest.of(0, 10));

        assertTrue(response.getData().isEmpty());
        assertEquals(0L, response.getPagination().getTotalElements());
    }

    @Test
    void paymentAccountReportShowsAllDrContraEntriesOfPaymentVoucher() {
        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                3L, LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 5), PageRequest.of(0, 10));

        assertEquals(2, response.getData().size(), "opening balance + bank's single entry for V8 expected");
        assertEquals("Opening Balance", response.getData().get(0).getLedgerName());

        AccountLedgerEntryItem item = response.getData().get(1);
        assertEquals("Rent, Electricity", item.getLedgerName());
        assertEquals(0, item.getCreditAmt().compareTo(BigDecimal.valueOf(8000)));

        assertEquals(2, item.getContraEntries().size());
        assertEquals("Rent", item.getContraEntries().get(0).getLedgerName());
        assertEquals(0, item.getContraEntries().get(0).getDebitAmt().compareTo(BigDecimal.valueOf(5000)));
        assertEquals("Electricity", item.getContraEntries().get(1).getLedgerName());
        assertEquals(0, item.getContraEntries().get(1).getDebitAmt().compareTo(BigDecimal.valueOf(3000)));
    }

    @Test
    void drLedgerReportShowsPaymentAccountAsContra() {
        AccountLedgerPageResponse response = ledgerReportService.getAccountLedgerEntries(
                5L, LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 5), PageRequest.of(0, 10));

        assertEquals(1, response.getData().size());

        AccountLedgerEntryItem item = response.getData().get(0);
        assertEquals("Bank", item.getLedgerName());
        assertEquals(0, item.getDebitAmt().compareTo(BigDecimal.valueOf(5000)));

        assertEquals(1, item.getContraEntries().size());
        assertEquals("Bank", item.getContraEntries().get(0).getLedgerName());
        assertEquals(0, item.getContraEntries().get(0).getCreditAmt().compareTo(BigDecimal.valueOf(8000)));
    }

    private void seed(Statement statement) throws Exception {
        statement.execute("""
                INSERT INTO account_group (id, name, nature, behaviour, shop_code)
                VALUES (1, 'Test Group', 'ASSET', 'DR', 1)
                """);
        statement.execute("""
                INSERT INTO ledger (id, name, legal_name, account_group_id, opening_balance,
                                    opening_balance_type, is_active, shop_code)
                VALUES (1, 'Customer A', NULL, 1, 1000.00, 'DR', TRUE, 1),
                       (2, 'Cash', NULL, 1, 0.00, 'DR', TRUE, 1),
                       (3, 'Bank', NULL, 1, 0.00, 'DR', TRUE, 1),
                       (4, 'Empty Ledger', NULL, 1, 0.00, 'DR', TRUE, 1),
                       (5, 'Rent', NULL, 1, 0.00, 'DR', TRUE, 1),
                       (6, 'Electricity', NULL, 1, 0.00, 'DR', TRUE, 1)
                """);

        // Ledger 1 net per voucher: V1 +500, V2 -200, V3 +300, V4 -100, V5 +50, V6 -60, V7 +70
        insertVoucher(statement, 1, "2026-04-10", "sale on credit",
                "500.00", "0.00", 2L, "0.00", "500.00");
        insertVoucher(statement, 2, "2026-04-20", "receipt from customer",
                "0.00", "200.00", 3L, "200.00", "0.00");
        insertVoucher(statement, 3, "2026-05-05", "more sales",
                "300.00", "0.00", 2L, "0.00", "300.00");
        insertVoucher(statement, 4, "2026-05-06", "partial receipt",
                "0.00", "100.00", 2L, "100.00", "0.00");
        insertVoucher(statement, 5, "2026-06-01", "sale day 1",
                "50.00", "0.00", 2L, "0.00", "50.00");
        insertVoucher(statement, 6, "2026-06-01", "sale day 1 second",
                "0.00", "60.00", 2L, "60.00", "0.00");
        insertVoucher(statement, 7, "2026-06-02", "sale next day",
                "70.00", "0.00", 2L, "0.00", "70.00");

        // Payment voucher V8 with multiple DR entries (Rent 5000, Electricity 3000) and one CR (Bank 8000)
        statement.execute("""
                INSERT INTO voucher (id, shop_code, voucher_type, voucher_number, voucher_date, narration)
                VALUES (8, 1, 'PAYMENT', 8, '2026-06-05', 'payment to vendor')
                """);
        insertEntry(statement, 8, 5L, 1, "5000.00", "0.00");
        insertEntry(statement, 8, 6L, 2, "3000.00", "0.00");
        insertEntry(statement, 8, 3L, 3, "0.00", "8000.00");
    }

    private void insertVoucher(Statement statement, long voucherId, String date, String narration,
                               String ledger1Debit, String ledger1Credit,
                               long contraLedgerId, String contraDebit, String contraCredit) throws Exception {
        statement.execute("""
                INSERT INTO voucher (id, shop_code, voucher_type, voucher_number, voucher_date, narration)
                VALUES (%d, 1, 'SALES', %d, '%s', '%s')
                """.formatted(voucherId, voucherId, date, narration));
        insertEntry(statement, voucherId, 1L, 1, ledger1Debit, ledger1Credit);
        insertEntry(statement, voucherId, contraLedgerId, 2, contraDebit, contraCredit);
    }

    private void insertEntry(Statement statement, long voucherId, long ledgerId, int line,
                             String debit, String credit) throws Exception {
        statement.execute("""
                INSERT INTO voucher_entry (shop_code, voucher_id, ledger_id, line_number,
                                           debit_amount, credit_amount)
                VALUES (1, %d, %d, %d, %s, %s)
                """.formatted(voucherId, ledgerId, line, debit, credit));
    }
}
