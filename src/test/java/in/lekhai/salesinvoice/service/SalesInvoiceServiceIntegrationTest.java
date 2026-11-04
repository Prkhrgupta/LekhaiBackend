package in.lekhai.salesinvoice.service;

import in.lekhai.contract.model.SalesInvoiceItemRequest;
import in.lekhai.contract.model.SalesInvoicePreviewResponse;
import in.lekhai.contract.model.SalesInvoiceRequest;
import in.lekhai.contract.model.SalesInvoiceResponse;
import in.lekhai.contract.model.SalesInvoiceWarningCode;
import in.lekhai.error.controller.LekhaiClientException;
import in.lekhai.shop.context.model.ShopContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {"spring.flyway.enabled=true"})
class SalesInvoiceServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SalesInvoiceService salesInvoiceService;

    @Autowired
    private StockLedgerService stockLedgerService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        ShopContext.setShopCode(1);
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").claim("financialYearStart", "2026")
                .claim("scope", "SHOP_OWNER").claim("shop_code", 1).claim("subject", "tester").build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("SET app.shop_code = '1'");
            statement.execute("""
                    TRUNCATE sales_invoice_tax, sales_invoice_item, sales_invoice, stock_ledger, voucher_entry,
                    voucher, custom_voucher_series, sale_ledger_setting_master, general_ledger_setting_master,
                    stock_item_master, commodity_master, uom_master, item_category_master, ledger_address,
                    ledger, account_group, shops, categories RESTART IDENTITY CASCADE
                    """);
            seed(statement);
        }
    }

    @AfterEach
    void tearDown() {
        ShopContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createsBalancedInvoiceWithStockOutAndNegativeStockWarning() {
        SalesInvoiceResponse response = salesInvoiceService.create(request(120.0, null));

        assertThat(response.getBillDisplay()).isEqualTo("GST/1");
        assertThat(response.getIsInterState()).isFalse();
        assertThat(response.getTotals().getTaxableAmount()).isEqualTo(28500.0);
        assertThat(response.getTotals().getNetAmount()).isEqualTo(29925.0);
        assertThat(response.getTaxes()).hasSize(2);
        assertThat(response.getWarnings()).extracting(w -> w.getCode())
                .containsExactly(SalesInvoiceWarningCode.NEGATIVE_STOCK);
        assertThat(debitSum(response.getVoucherId())).isEqualByComparingTo(creditSum(response.getVoucherId()));
        assertThat(stockLedgerService.getAvailability(1L).getAvailableQty()).isEqualTo(-20.0);
    }

    @Test
    void secondInvoiceGetsNextBillNoAndPreviewDoesNotConsumeNumber() {
        SalesInvoicePreviewResponse preview = salesInvoiceService.preview(request(10.0, null));
        assertThat(preview.getBillDisplay()).isEqualTo("GST/1");

        salesInvoiceService.create(request(10.0, null));
        SalesInvoiceResponse second = salesInvoiceService.create(request(10.0, null));

        assertThat(second.getBillDisplay()).isEqualTo("GST/2");
        assertThat(second.getWarnings()).isEmpty();
    }

    @Test
    void interStateWithoutOutStateSaleSettingIsRejected() {
        assertThatThrownBy(() -> salesInvoiceService.preview(request(10.0, "29")))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("out-of-state sale ledger");
    }

    @Test
    void shipToIdIsRejectedUntilShipToTableExists() {
        SalesInvoiceRequest request = request(10.0, null).shipToId(5L);

        assertThatThrownBy(() -> salesInvoiceService.create(request))
                .isInstanceOf(LekhaiClientException.class)
                .hasMessageContaining("Ship-to");
    }

    @Test
    void appObjectMapperReadsNullableContractFields() throws Exception {
        String json = """
                {"series_code":"GST","bill_date":"2026-11-04","party_ledger_id":1,
                 "place_of_supply":"27","bill_no":5,"remarks":null,
                 "items":[{"stock_item_id":1,"quantity":2,"rate":10}]}
                """;

        SalesInvoiceRequest request = objectMapper.readValue(json, SalesInvoiceRequest.class);

        assertThat(request.getPlaceOfSupply().get()).isEqualTo("27");
        assertThat(request.getBillNo().get()).isEqualTo(5L);
        assertThat(request.getRemarks().isPresent()).isTrue();
        assertThat(request.getShipToId().isPresent()).isFalse();
    }

    private SalesInvoiceRequest request(double qty, String placeOfSupply) {
        SalesInvoiceRequest request = new SalesInvoiceRequest("GST", LocalDate.of(2026, 11, 4), 1L,
                java.util.List.of(new SalesInvoiceItemRequest(1L, qty, 250.0).discountPct(5.0)));
        return placeOfSupply == null ? request : request.placeOfSupply(placeOfSupply);
    }

    private java.math.BigDecimal debitSum(Long voucherId) {
        return new JdbcTemplate(dataSource).queryForObject(
                "SELECT COALESCE(SUM(debit_amount),0) FROM voucher_entry WHERE voucher_id = ?",
                java.math.BigDecimal.class, voucherId);
    }

    private java.math.BigDecimal creditSum(Long voucherId) {
        return new JdbcTemplate(dataSource).queryForObject(
                "SELECT COALESCE(SUM(credit_amount),0) FROM voucher_entry WHERE voucher_id = ?",
                java.math.BigDecimal.class, voucherId);
    }

    private void seed(Statement s) throws Exception {
        s.execute("""
                INSERT INTO state (state_code, state_name, gst_code, type)
                VALUES ('MH','Maharashtra','27','State'), ('KA','Karnataka','29','State')
                ON CONFLICT DO NOTHING
                """);
        s.execute("INSERT INTO categories (id, name) VALUES (1, 'SAREE')");
        s.execute("""
                INSERT INTO shops (shop_code, firm_name, category_id, state_code)
                VALUES (1, 'Test Firm', 1, 'MH')
                """);
        s.execute("""
                INSERT INTO account_group (id, name, nature, behaviour, shop_code) VALUES
                (1, 'Sundry Debtors', 'ASSET', 'DR', 1), (2, 'Sales Account', 'INCOME', 'CR', 1),
                (3, 'Duties & Taxes', 'LIABILITY', 'CR', 1), (4, 'Indirect Expenses', 'EXPENSE', 'DR', 1)
                """);
        s.execute("""
                INSERT INTO ledger (id, name, account_group_id, opening_balance, opening_balance_type, shop_code)
                VALUES (1, 'Customer A', 1, 0, 'DR', 1), (2, 'Sales 5%', 2, 0, 'CR', 1),
                       (3, 'CGST 2.5%', 3, 0, 'CR', 1), (4, 'SGST 2.5%', 3, 0, 'CR', 1),
                       (5, 'Round Off', 4, 0, 'DR', 1)
                """);
        s.execute("INSERT INTO ledger_address (ledger_id, city, state_id, shop_code) VALUES (1, 'Mumbai', 'MH', 1)");
        s.execute("""
                INSERT INTO sale_ledger_setting_master (sale_ledger_id, sale_type, gst_rate, cgst_ledger_id,
                                                        sgst_ledger_id, shop_code)
                VALUES (2, 'IN_STATE', 5, 3, 4, 1)
                """);
        s.execute("INSERT INTO general_ledger_setting_master (round_off_ledger_id, shop_code) VALUES (5, 1)");
        s.execute("""
                INSERT INTO custom_voucher_series (voucher_type, series_code, financial_year, prefix, shop_code)
                VALUES ('SALES', 'GST', '2026-27', 'GST/', 1)
                """);
        s.execute("INSERT INTO uom_master (id, unit_name, quantity_code, shop_code) VALUES (1, 'Meter', 'MTR', 1)");
        s.execute("INSERT INTO item_category_master (id, name, shop_code) VALUES (1, 'Shirting', 1)");
        s.execute("""
                INSERT INTO commodity_master (item_id, item_name, hsn_sac_code, gst_rate, shop_code)
                VALUES (1, 'Cotton', '5208', 5, 1)
                """);
        s.execute("""
                INSERT INTO stock_item_master (id, item_name, item_category_id, commodity_id, primary_uom_id,
                                               purchase_price, opening_qty, shop_code)
                VALUES (1, 'Cotton Shirting', 1, 1, 1, 200, 100, 1)
                """);
    }
}
