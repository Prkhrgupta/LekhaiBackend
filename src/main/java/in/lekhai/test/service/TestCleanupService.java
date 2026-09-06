package in.lekhai.test.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "lekhai.test.endpoints.enabled", havingValue = "true")
public class TestCleanupService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JdbcTemplate jdbcTemplate;

    public TestCleanupService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void cleanupShop(Integer shopCode) {
        log.info("Cleaning up test data for shopCode: {}", shopCode);
        jdbcTemplate.update("DELETE FROM user_shop_access WHERE shop_id IN (SELECT id FROM shops WHERE shop_code = ?)", shopCode);
        jdbcTemplate.update("DELETE FROM shops WHERE shop_code = ?", shopCode);
    }

    @Transactional
    public void cleanupUser(String username) {
        log.info("Cleaning up test user: {}", username);
        jdbcTemplate.update("DELETE FROM user_shop_access WHERE user_id IN (SELECT id FROM users WHERE uuid IN (SELECT uuid FROM user_accounts WHERE username = ?))", username);
        jdbcTemplate.update("DELETE FROM users WHERE uuid IN (SELECT uuid FROM user_accounts WHERE username = ?)", username);
        jdbcTemplate.update("DELETE FROM super_admin_details WHERE uuid IN (SELECT uuid FROM user_accounts WHERE username = ?)", username);
        jdbcTemplate.update("DELETE FROM user_accounts WHERE username = ?", username);
    }

    @Transactional
    public void cleanupAllTestData() {
        log.info("Executing comprehensive test data cleanup (preserving super_admin)");
        jdbcTemplate.update("DELETE FROM user_shop_access WHERE user_id NOT IN (SELECT u.id FROM users u JOIN user_accounts a ON u.uuid = a.uuid WHERE a.username = 'admin')");
        jdbcTemplate.update("DELETE FROM shops");
        jdbcTemplate.update("DELETE FROM users WHERE uuid NOT IN (SELECT uuid FROM user_accounts WHERE username = 'admin')");
        jdbcTemplate.update("DELETE FROM super_admin_details WHERE uuid NOT IN (SELECT uuid FROM user_accounts WHERE username = 'admin')");
        jdbcTemplate.update("DELETE FROM user_accounts WHERE username != 'admin'");
    }
}
