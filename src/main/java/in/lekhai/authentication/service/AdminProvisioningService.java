package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.authentication.repository.UserCredentialRepository;
import in.lekhai.core.entity.SuperAdminMaster;
import in.lekhai.core.model.enums.Roles;
import in.lekhai.core.repository.SuperAdminMasterRepo;
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
    private final UserCredentialRepository userCredentialRepository;
    private final SuperAdminMasterRepo superAdminMasterRepo;

    private final String username;
    private final String password;
    private final String name;

    public AdminProvisioningService(
            PasswordEncoder passwordEncoder,
            UserCredentialRepository userCredentialRepository,
            SuperAdminMasterRepo superAdminMasterRepo,
            @Value("${super-admin.username}") String username,
            @Value("${super-admin.password}") String password,
            @Value("${super-admin.name}") String name
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userCredentialRepository = userCredentialRepository;
        this.superAdminMasterRepo = superAdminMasterRepo;
        this.username = username;
        this.password = password;
        this.name = name;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void provisionSuperAdmin() {
        Optional<UserCredentials> userCredentials = userCredentialRepository.findByUsername(username);
        if(userCredentials.isPresent()) {
            log.info("SUPER ADMIN ALREADY {} EXISTS", username);
            return;
        }

        UserCredentials superAdminCredentials = UserCredentials.builder()
                .username(username)
                .passHash(passwordEncoder.encode(password))
                .uuid(createUUID(Roles.SUPER_ADMIN))
                .build();
        UserCredentials userCredentialsSaved = userCredentialRepository.save(superAdminCredentials);

        SuperAdminMaster superAdminMaster = SuperAdminMaster.builder()
                .name(name)
                .uuid(userCredentialsSaved.getUuid())
                .build();

        superAdminMasterRepo.save(superAdminMaster);
        log.info("CREATED SUPER ADMIN {}", username);
    }
}
