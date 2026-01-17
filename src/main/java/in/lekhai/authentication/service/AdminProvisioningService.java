package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.superadmin.SuperAdminDetails;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.superadmin.SuperAdminDetailsRepo;
import in.lekhai.core.repository.users.UserShopAccessRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static in.lekhai.core.util.AdminUtils.createUUID;

@Service
public class AdminProvisioningService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PasswordEncoder passwordEncoder;
    private final UserAccountRepository userAccountRepository;
    private final SuperAdminDetailsRepo superAdminDetailsRepo;

    private final String username;
    private final String password;
    private final String name;

    public AdminProvisioningService(
            PasswordEncoder passwordEncoder,
            UserAccountRepository userAccountRepository,
            SuperAdminDetailsRepo superAdminDetailsRepo,
            ShopsRepo shopsRepo,
            UserShopAccessRepo userShopAccessRepo,
            @Value("${super-admin.username}") String username,
            @Value("${super-admin.password}") String password,
            @Value("${super-admin.name}") String name) {
        this.passwordEncoder = passwordEncoder;
        this.userAccountRepository = userAccountRepository;
        this.superAdminDetailsRepo = superAdminDetailsRepo;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void provisionSuperAdmin() {
        Optional<UserAccounts> userCredentials = userAccountRepository.findByUsername(username);
        if (userCredentials.isPresent()) {
            log.info("SUPER ADMIN ALREADY {} EXISTS", username);
            return;
        }

        String uuid = createUUID(Roles.SUPER_ADMIN);
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
}
