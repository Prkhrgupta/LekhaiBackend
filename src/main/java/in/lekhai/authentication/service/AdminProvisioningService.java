package in.lekhai.authentication.service;

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

import java.util.Optional;
import java.util.Set;

import static in.lekhai.core.util.AdminUtils.createUUID;

@Service
public class AdminProvisioningService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final SuperAdminDetailsRepo superAdminDetailsRepo;
    private final CategoriesRepo categoriesRepo;
    private final RolePermissionsRepo rolePermissionsRepo;
    private final UsersRepo usersRepo;
    private final PermissionBitCalculator permissionBitCalculator;

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
        this.username = username;
        this.password = password;
        this.name = name;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void provisionSuperAdmin() {
        Categories superAdminCategory = categoriesRepo.findByName("SUPER_ADMIN")
                .orElseGet(() -> {
                    Categories cat = Categories.builder()
                            .name("SUPER_ADMIN")
                            .build();
                    return categoriesRepo.save(cat);
                });

        if (superAdminCategory.getPermissions() == null || superAdminCategory.getPermissions().isEmpty()) {
            permissionBitCalculator.enableBits(superAdminCategory.getPermissions(), Set.of(29));
            superAdminCategory = categoriesRepo.save(superAdminCategory);
        }

        if (rolePermissionsRepo.findByCategoryIdAndRoleId(superAdminCategory.getId(), Roles.SUPER_ADMIN).isEmpty()) {
            RolePermissions rolePerm = new RolePermissions(Roles.SUPER_ADMIN, superAdminCategory.getId());
            permissionBitCalculator.enableBits(rolePerm.getPermissions(), Set.of(29));
            rolePermissionsRepo.save(rolePerm);
        }

        Optional<UserAccounts> userCredentials = userAccountRepository.findByUsername(username);
        String uuid;
        if (userCredentials.isPresent()) {
            uuid = userCredentials.get().getUuid();
            log.info("SUPER ADMIN ALREADY {} EXISTS", username);
        } else {
            uuid = createUUID(Roles.SUPER_ADMIN);
            UserAccounts superAdminCredentials = UserAccounts.builder()
                    .username(username)
                    .passHash(passwordEncoder.encode(password))
                    .uuid(uuid)
                    .build();
            userAccountRepository.save(superAdminCredentials);

            SuperAdminDetails superAdmin = new SuperAdminDetails();
            superAdmin.setName(name);
            superAdmin.setUuid(uuid);
            superAdminDetailsRepo.save(superAdmin);

            log.info("CREATED SUPER ADMIN {}", username);
        }

        Optional<Users> userOpt = usersRepo.findByUuid(uuid);
        if (userOpt.isEmpty()) {
            Users adminUser = Users.createUserWithCategory(uuid, name, superAdminCategory.getId());
            usersRepo.save(adminUser);
        } else if (userOpt.get().getCategoryId() == null) {
            Users adminUser = userOpt.get();
            adminUser.setCategoryId(superAdminCategory.getId());
            usersRepo.save(adminUser);
        }
    }
}
