package in.lekhai.core.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.core.entity.SuperAdminMaster;
import in.lekhai.core.repository.SuperAdminMasterRepo;
import in.lekhai.exception.controller.exception.CategoryDoesNotExistException;
import in.lekhai.exception.controller.exception.UsernameAlreadyExistException;
import in.lekhai.core.model.enums.Roles;
import in.lekhai.authentication.repository.UserCredentialRepository;
import in.lekhai.core.entity.CategoryMaster;
import in.lekhai.core.entity.RoleCategoryMaster;
import in.lekhai.core.entity.TenantDetails;
import in.lekhai.core.model.request.AdminRegistrationRequest;
import in.lekhai.core.model.request.SuperAdminRegistrationRequest;
import in.lekhai.core.model.response.AdminRegistrationResponse;
import in.lekhai.core.model.request.CategoryCreationRequest;
import in.lekhai.core.model.response.SuperAdminRegistrationResponse;
import in.lekhai.core.repository.CategoryMasterRepo;
import in.lekhai.core.repository.RoleCategoryMasterRepo;
import in.lekhai.core.repository.TenantDetailsRepo;
import in.lekhai.core.util.AdminUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static in.lekhai.core.util.AdminUtils.createUUID;

@Service
@Slf4j
public class SuperAdminService {

    private final CategoryMasterRepo categoryMasterRepo;
    private final RoleCategoryMasterRepo roleCategoryMasterRepo;
    private final UserCredentialRepository userCredentialRepository;
    private final TenantDetailsRepo tenantDetailsRepo;
    private final PasswordEncoder passwordEncoder;
    private final SuperAdminMasterRepo superAdminMasterRepo;

    public SuperAdminService(
            CategoryMasterRepo categoryMasterRepo,
            RoleCategoryMasterRepo roleCategoryMasterRepo,
            UserCredentialRepository userCredentialRepository,
            TenantDetailsRepo tenantDetailsRepo,
            PasswordEncoder passwordEncoder,
            SuperAdminMasterRepo superAdminMasterRepo
    ) {
        this.categoryMasterRepo = categoryMasterRepo;
        this.roleCategoryMasterRepo = roleCategoryMasterRepo;
        this.userCredentialRepository = userCredentialRepository;
        this.tenantDetailsRepo = tenantDetailsRepo;
        this.passwordEncoder = passwordEncoder;
        this.superAdminMasterRepo = superAdminMasterRepo;
    }

    @Transactional
    public SuperAdminRegistrationResponse registerSuperAdmin(SuperAdminRegistrationRequest request) {
        Optional<UserCredentials> userCredentials = userCredentialRepository.findByUsername(request.username());
        if(userCredentials.isPresent()) {
            throw new UsernameAlreadyExistException(request.username());
        }

        UserCredentials superAdminRegistrationResponse = userCredentialRepository.save(
                UserCredentials.builder()
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

    @Transactional
    public AdminRegistrationResponse registerAdmin(AdminRegistrationRequest request) {
        CategoryMaster category = categoryMasterRepo
                .findByCategory(request.category())
                .orElseThrow(() -> new CategoryDoesNotExistException(request.category()));

        Optional<UserCredentials> userCredentials = userCredentialRepository.findByUsername(request.username());
        if(userCredentials.isPresent()) {
            throw new UsernameAlreadyExistException(request.username());
        }

        RoleCategoryMaster roleCategoryMaster = roleCategoryMasterRepo
                .findByRoleAndCategoryId(category.getId(), Roles.ADMIN)
                .orElseGet(() -> {
                    RoleCategoryMaster adminEntryForCategory = RoleCategoryMaster.builder()
                            .categoryId(category.getId())
                            .role(Roles.ADMIN)
                            .permission(category.getPermission())
                            .build();
                    return roleCategoryMasterRepo.save(adminEntryForCategory);
                });

        UserCredentials registeredAdmin = userCredentialRepository.save(UserCredentials.builder()
                        .uuid(AdminUtils.createUUID(roleCategoryMaster.getRole()))
                        .username(request.username())
                        .passHash(passwordEncoder.encode(request.password()))
                        .build());

        TenantDetails registeredTenant = tenantDetailsRepo.save(TenantDetails.builder()
                .firmName(request.firmName())
                .role(roleCategoryMaster.getRole())
                .uuid(registeredAdmin.getUuid())
                .uuid(registeredAdmin.getUuid())
                .categoryId(category.getId())
                .gstIn(request.gstIn())
                .permissionBit(category.getPermission())
                .tenant(AdminUtils.createTenant())
                .build());

        return new AdminRegistrationResponse(
                registeredAdmin.getUsername(),
                registeredTenant.getFirmName(),
                category.getCategory(),
                registeredTenant.getGstIn(),
                registeredTenant.getTenant()
        );
    }

    public void createCategory(CategoryCreationRequest request) {
        CategoryMaster toBeSavedCategory = CategoryMaster.builder()
                .category(request.categoryName())
                .build();
        categoryMasterRepo.save(toBeSavedCategory);
    }
}
