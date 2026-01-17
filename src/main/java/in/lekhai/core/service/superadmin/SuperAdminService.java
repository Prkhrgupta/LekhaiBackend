package in.lekhai.core.service.superadmin;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.superadmin.SuperAdminDetails;
import in.lekhai.core.dto.superadmin.SuperAdminRegistrationRequest;
import in.lekhai.core.dto.superadmin.SuperAdminRegistrationResponse;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.superadmin.SuperAdminDetailsRepo;
import in.lekhai.error.controller.user.exception.UserAlreadyExistException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static in.lekhai.core.util.AdminUtils.createUUID;

@Service
public class SuperAdminService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminDetailsRepo superAdminDetailsRepo;

    public SuperAdminService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            SuperAdminDetailsRepo superAdminDetailsRepo) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.superAdminDetailsRepo = superAdminDetailsRepo;
    }

    @Transactional
    public SuperAdminRegistrationResponse registerSuperAdmin(SuperAdminRegistrationRequest request) {
        userAccountRepository
                .findByUsername(request.username())
                .ifPresent(user -> {
                    throw new UserAlreadyExistException(request.username());
                });

        UserAccounts superAdminRegistrationResponse = userAccountRepository.save(
                UserAccounts.builder()
                        .username(request.username())
                        .passHash(passwordEncoder.encode(request.password()))
                        .uuid(createUUID(Roles.SUPER_ADMIN))
                        .build());

        SuperAdminDetails superAdmin = new SuperAdminDetails();
        superAdmin.setName(request.name());
        superAdmin.setUuid(createUUID(Roles.SUPER_ADMIN));

        superAdminDetailsRepo.save(superAdmin);

        return new SuperAdminRegistrationResponse(superAdmin.getName(),
                superAdminRegistrationResponse.getUsername());
    }
}
