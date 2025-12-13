package in.lekhai.authentication.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.authentication.repository.UserCredentialRepository;
import in.lekhai.core.model.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static in.lekhai.core.util.AdminUtils.createUUID;

@Service
public class AdminProvisioningService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final PasswordEncoder passwordEncoder;
    private final UserCredentialRepository userCredentialRepository;

    private final String username;
    private final String password;

    public AdminProvisioningService(
            PasswordEncoder passwordEncoder,
            UserCredentialRepository userCredentialRepository,
            @Value("${super-admin.username}") String username,
            @Value("${super-admin.password}") String password
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userCredentialRepository = userCredentialRepository;
        this.username = username;
        this.password = password;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void provisionSuperAdmin() {
        Optional<UserCredentials> userCredentials = userCredentialRepository.findByUsername(username);
        if(userCredentials.isPresent()) {
            log.info("SUPER ADMIN ALREADY EXISTS");
            return;
        }

        UserCredentials superAdminCredentials = UserCredentials.builder()
                .username(username)
                .passHash(passwordEncoder.encode(password))
                .uuid(createUUID(Roles.SUPER_ADMIN))
                .build();

        userCredentialRepository.save(superAdminCredentials);
        log.info("CREATING SUPER ADMIN");
    }
}
