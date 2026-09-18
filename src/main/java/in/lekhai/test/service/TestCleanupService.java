package in.lekhai.test.service;

import in.lekhai.authentication.service.AdminProvisioningService;
import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.util.PermissionBitCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@ConditionalOnProperty(name = "lekhai.test.endpoints.enabled", havingValue = "true")
public class TestCleanupService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JdbcTemplate jdbcTemplate;
    private final CategoriesRepo categoriesRepo;
    private final RolePermissionsRepo rolePermissionsRepo;
    private final PermissionBitCalculator permissionBitCalculator;
    private final AdminProvisioningService adminProvisioningService;

    public TestCleanupService(
            JdbcTemplate jdbcTemplate,
            CategoriesRepo categoriesRepo,
            RolePermissionsRepo rolePermissionsRepo,
            PermissionBitCalculator permissionBitCalculator,
            AdminProvisioningService adminProvisioningService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.categoriesRepo = categoriesRepo;
        this.rolePermissionsRepo = rolePermissionsRepo;
        this.permissionBitCalculator = permissionBitCalculator;
        this.adminProvisioningService = adminProvisioningService;
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
        log.info("Executing comprehensive test data cleanup");
        jdbcTemplate.execute("SET LOCAL app.bypass_rls = 'on'");
        jdbcTemplate.update("DELETE FROM voucher_entry");
        jdbcTemplate.update("DELETE FROM voucher");
        jdbcTemplate.update("DELETE FROM voucher_counter");
        jdbcTemplate.update("DELETE FROM stock_item_master");
        jdbcTemplate.update("DELETE FROM item_factory_master");
        jdbcTemplate.update("DELETE FROM item_category_master");
        jdbcTemplate.update("DELETE FROM commodity_master");
        jdbcTemplate.update("DELETE FROM gst_details");
        jdbcTemplate.update("DELETE FROM ledger_address");
        jdbcTemplate.update("DELETE FROM ledger");
        jdbcTemplate.update("DELETE FROM broker");
        jdbcTemplate.update("DELETE FROM transport");
        jdbcTemplate.update("DELETE FROM area");
        jdbcTemplate.update("DELETE FROM user_shop_access WHERE user_id NOT IN (SELECT u.id FROM users u JOIN user_accounts a ON u.uuid = a.uuid WHERE a.username = 'admin')");
        jdbcTemplate.update("DELETE FROM shops");
        jdbcTemplate.update("DELETE FROM users WHERE uuid NOT IN (SELECT uuid FROM user_accounts WHERE username = 'admin')");
        jdbcTemplate.update("DELETE FROM super_admin_details WHERE uuid NOT IN (SELECT uuid FROM user_accounts WHERE username = 'admin')");
        jdbcTemplate.update("DELETE FROM user_accounts WHERE username != 'admin'");
        jdbcTemplate.update("DELETE FROM role_permissions WHERE category_id NOT IN (SELECT id FROM categories WHERE name IN ('SAREE', 'SUPER_ADMIN'))");
        jdbcTemplate.update("DELETE FROM categories WHERE name NOT IN ('SAREE', 'SUPER_ADMIN')");
        jdbcTemplate.update("DELETE FROM features WHERE feature_key = 'FEATURE_INVOICE_EXPORT_TEST'");

        // Ensure default SAREE / SHOP_ADMIN category exists for tests
        Categories sareeCategory = categoriesRepo.findByName("SAREE")
                .orElseGet(() -> categoriesRepo.save(Categories.builder().name("SAREE").build()));

        if (sareeCategory.getPermissions() == null || sareeCategory.getPermissions().isEmpty()) {
            permissionBitCalculator.enableBits(sareeCategory.getPermissions(), Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29));
            sareeCategory = categoriesRepo.save(sareeCategory);
        }

        if (rolePermissionsRepo.findByCategoryIdAndRoleId(sareeCategory.getId(), Roles.SHOP_OWNER).isEmpty()) {
            RolePermissions rolePerm = new RolePermissions(Roles.SHOP_OWNER, sareeCategory.getId());
            permissionBitCalculator.enableBits(rolePerm.getPermissions(), Set.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29));
            rolePermissionsRepo.save(rolePerm);
        }

        adminProvisioningService.provisionSuperAdmin();
    }
}
