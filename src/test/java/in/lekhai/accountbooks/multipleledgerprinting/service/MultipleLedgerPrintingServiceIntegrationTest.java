package in.lekhai.accountbooks.multipleledgerprinting.service;

import in.lekhai.contract.model.MultipleLedgerPrintingResponse;
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
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true"
})
class MultipleLedgerPrintingServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MultipleLedgerPrintingService service;

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        ShopContext.setShopCode(1);
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET app.shop_code = '1'");
                statement.execute("TRUNCATE ledger, account_group RESTART IDENTITY CASCADE");
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
                (1, 'Capital Account', NULL, 'LIABILITY', 'CR', true, 1),
                (2, 'Loans (Liability)', NULL, 'LIABILITY', 'CR', true, 1),
                (17, 'Secured Loans', 2, 'LIABILITY', 'CR', false, 1),
                (4, 'Fixed Assets', NULL, 'ASSET', 'DR', true, 1);
        """);

        statement.execute("""
            INSERT INTO ledger (id, name, account_group_id, opening_balance, opening_balance_type, is_active, shop_code)
            VALUES
                (1, 'NARAIN DAS OMAR "PROP"', 1, 0.00, 'CR', true, 1),
                (2, 'BUSINESS LOAN "MSME A/C"', 17, 0.00, 'CR', true, 1),
                (3, 'CAR', 4, 0.00, 'DR', true, 1),
                (4, 'AIR CONDITIONER', 4, 0.00, 'DR', true, 1);
        """);
    }

    @Test
    void testGetMultipleLedgerPrinting_IntegrationFlow() {
        MultipleLedgerPrintingResponse response = service.getMultipleLedgerPrinting();

        assertNotNull(response);
        assertEquals("Index for Account Ledgers", response.getTitle());
        assertEquals(4, response.getTotalLedgers());
        assertEquals(7, response.getItems().size());

        // Capital Account
        assertTrue(response.getItems().get(0).getIsGroup());
        assertEquals("Capital Account", response.getItems().get(0).getParticulars());

        assertFalse(response.getItems().get(1).getIsGroup());
        assertEquals(1, response.getItems().get(1).getSerialNumber().get());
        assertEquals("NARAIN DAS OMAR \"PROP\"", response.getItems().get(1).getParticulars());

        // Secured Loans
        assertTrue(response.getItems().get(2).getIsGroup());
        assertEquals("Secured Loans", response.getItems().get(2).getParticulars());

        assertFalse(response.getItems().get(3).getIsGroup());
        assertEquals(2, response.getItems().get(3).getSerialNumber().get());
        assertEquals("BUSINESS LOAN \"MSME A/C\"", response.getItems().get(3).getParticulars());

        // Fixed Assets
        assertTrue(response.getItems().get(4).getIsGroup());
        assertEquals("Fixed Assets", response.getItems().get(4).getParticulars());

        // Alphabetical inside group: AIR CONDITIONER before CAR
        assertFalse(response.getItems().get(5).getIsGroup());
        assertEquals(3, response.getItems().get(5).getSerialNumber().get());
        assertEquals("AIR CONDITIONER", response.getItems().get(5).getParticulars());

        assertFalse(response.getItems().get(6).getIsGroup());
        assertEquals(4, response.getItems().get(6).getSerialNumber().get());
        assertEquals("CAR", response.getItems().get(6).getParticulars());
    }
}
