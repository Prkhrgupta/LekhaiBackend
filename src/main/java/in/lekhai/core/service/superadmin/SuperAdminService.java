package in.lekhai.core.service.superadmin;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.superadmin.SuperAdminMaster;
import in.lekhai.core.dto.superadmin.SuperAdminRegistrationRequest;
import in.lekhai.core.dto.superadmin.SuperAdminRegistrationResponse;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.admin.AdminDetailsRepo;
import in.lekhai.core.repository.category.CategoryMasterRepo;
import in.lekhai.core.repository.category.RoleCategoryMasterRepo;
import in.lekhai.core.repository.superadmin.SuperAdminMasterRepo;
import in.lekhai.core.repository.tenant.TenantDetailsRepo;
import in.lekhai.core.service.admin.AdminService;
import in.lekhai.error.controller.user.exception.UserAlreadyExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static in.lekhai.core.util.AdminUtils.createUUID;

@Service
@Slf4j
public class SuperAdminService {

    private final CategoryMasterRepo categoryMasterRepo;
    private final RoleCategoryMasterRepo roleCategoryMasterRepo;
    private final UserAccountRepository userAccountRepository;
    private final TenantDetailsRepo tenantDetailsRepo;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminMasterRepo superAdminMasterRepo;
    private final AdminService adminService;
    private final AdminDetailsRepo adminDetailsRepo;

    public SuperAdminService(
            CategoryMasterRepo categoryMasterRepo,
            RoleCategoryMasterRepo roleCategoryMasterRepo,
            UserAccountRepository userAccountRepository,
            TenantDetailsRepo tenantDetailsRepo,
            PasswordEncoder passwordEncoder,
            SuperAdminMasterRepo superAdminMasterRepo,
            AdminService adminService,
            AdminDetailsRepo adminDetailsRepo
    ) {
        this.categoryMasterRepo = categoryMasterRepo;
        this.roleCategoryMasterRepo = roleCategoryMasterRepo;
        this.userAccountRepository = userAccountRepository;
        this.tenantDetailsRepo = tenantDetailsRepo;
        this.passwordEncoder = passwordEncoder;
        this.superAdminMasterRepo = superAdminMasterRepo;
        this.adminService = adminService;
        this.adminDetailsRepo = adminDetailsRepo;
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
                        .passHash(passwordEncoder.encode(request.password())).
                        uuid(createUUID(Roles.SUPER_ADMIN))
                        .build()
        );

        SuperAdminMaster superAdminMasterResponse = superAdminMasterRepo.save(
                SuperAdminMaster.builder()
                        .name(request.name())
                        .uuid(superAdminRegistrationResponse.getUuid())
                        .build()
        );

        return new SuperAdminRegistrationResponse(superAdminMasterResponse.getName(),
                superAdminRegistrationResponse.getUsername());
    }
}
