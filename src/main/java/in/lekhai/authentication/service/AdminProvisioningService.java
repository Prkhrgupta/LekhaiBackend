package in.lekhai.authentication.service;

import in.lekhai.authentication.config.properties.SuperAdminRoutesProperties;
import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.domain.superadmin.SuperAdminDetails;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.repository.superadmin.SuperAdminDetailsRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.util.PermissionBitCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static in.lekhai.core.util.AdminUtils.createUUID;

@Service
public class AdminProvisioningService {

    private static final String SUPER_ADMIN_CATEGORY = "SUPER_ADMIN";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final SuperAdminDetailsRepo superAdminDetailsRepo;
    private final CategoriesRepo categoriesRepo;
    private final RolePermissionsRepo rolePermissionsRepo;
    private final UsersRepo usersRepo;
    private final PermissionBitCalculator permissionBitCalculator;
    private final SuperAdminRouteProvisioner routeProvisioner;
    private final SuperAdminRoutesProperties routesProperties;

    private final String username;
    private final String password;
    private final String name;

    public AdminProvisioningService(
            PasswordEncoder passwordEncoder,
            UserAccountRepository userAccountRepository,
            SuperAdminDetailsRepo superAdminDetailsRepo,
            CategoriesRepo categoriesRepo,
            RolePermissionsRepo rolePermissions,
            UsersRepo usersRepo,
            PermissionBitCalculator permissionBitCalculator,
            SuperAdminRouteProvisioner routeProvisioner,
            SuperAdminRoutesProperties routesProperties,
            @Value("${super-admin.username}") String username,
            @Value("${super-admin.password}") String password,
            @Value("${super-admin.name}") String name) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
        this.superAdminDetailsRepo = superAdminDetailsRepo;
        this.categoriesRepo = categoriesRepo;
        this.rolePermissionsRepo = rolePermissions;
        this.usersRepo = usersRepo;
        this.permissionBitCalculator = permissionBitCalculator;
        this.routeProvisioner = routeProvisioner;
        this.routesProperties = routesProperties;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    /**
     * Idempotent. Runs on every startup so routes added to config later are picked up.
     * Only the default super admin's category (SUPER_ADMIN) receives the route bits.
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void provisionSuperAdmin() {
        Categories superAdminCategory = ensureCategory();
        String uuid = ensureAccount();
        ensureUser(uuid, superAdminCategory.getId());

        Set<Integer> routeBits = routeProvisioner.ensureRoutes(routesProperties.routes());
        grantToCategory(superAdminCategory, routeBits);
        grantToRole(superAdminCategory.getId(), routeBits);
    }

    private Categories ensureCategory() {
        return categoriesRepo.findByName(SUPER_ADMIN_CATEGORY)
                .orElseGet(() -> categoriesRepo.save(Categories.builder().name(SUPER_ADMIN_CATEGORY).build()));
    }

    private void grantToCategory(Categories category, Set<Integer> bits) {
        if (enableMissingBits(category.getPermissions(), bits)) {
            categoriesRepo.save(category);
        }
    }

    private void grantToRole(Integer categoryId, Set<Integer> bits) {
        Optional<RolePermissions> existing = rolePermissionsRepo
                .findByCategoryIdAndRoleId(categoryId, Roles.SUPER_ADMIN);
        // why: the row must exist even with no routes, otherwise menu generation fails for this role
        RolePermissions rolePerm = existing.orElseGet(() -> new RolePermissions(Roles.SUPER_ADMIN, categoryId));
        boolean changed = enableMissingBits(rolePerm.getPermissions(), bits);
        if (existing.isEmpty() || changed) {
            rolePermissionsRepo.save(rolePerm);
        }
    }

    private boolean enableMissingBits(List<Long> permissions, Set<Integer> bits) {
        if (permissionBitCalculator.extractEnabledBitPositions(permissions).containsAll(bits)) {
            return false;
        }
        permissionBitCalculator.enableBits(permissions, bits);
        return true;
    }

    private String ensureAccount() {
        Optional<UserAccounts> userCredentials = userAccountRepository.findByUsername(username);
        if (userCredentials.isPresent()) {
            log.info("SUPER ADMIN ALREADY {} EXISTS", username);
            return userCredentials.get().getUuid();
        }
        String uuid = createUUID(Roles.SUPER_ADMIN);
        userAccountRepository.save(UserAccounts.builder()
                .username(username)
                .passHash(passwordEncoder.encode(password))
                .uuid(uuid)
                .build());

        SuperAdminDetails superAdmin = new SuperAdminDetails();
        superAdmin.setName(name);
        superAdmin.setUuid(uuid);
        superAdminDetailsRepo.save(superAdmin);

        log.info("CREATED SUPER ADMIN {}", username);
        return uuid;
    }

    private void ensureUser(String uuid, Integer categoryId) {
        Optional<Users> userOpt = usersRepo.findByUuid(uuid);
        if (userOpt.isEmpty()) {
            usersRepo.save(Users.createUserWithCategory(uuid, name, categoryId));
        } else if (userOpt.get().getCategoryId() == null) {
            Users adminUser = userOpt.get();
            adminUser.setCategoryId(categoryId);
            usersRepo.save(adminUser);
        }
    }
}
