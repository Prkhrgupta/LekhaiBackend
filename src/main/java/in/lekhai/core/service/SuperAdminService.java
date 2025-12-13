package in.lekhai.core.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.authentication.repository.UserCredentialRepository;
import in.lekhai.core.entity.CategoryMaster;
import in.lekhai.core.entity.RoleCategoryMaster;
import in.lekhai.core.entity.TenantDetails;
import in.lekhai.core.model.AdminRegistrationRequest;
import in.lekhai.core.model.AdminRegistrationResponse;
import in.lekhai.core.model.CategoryCreationRequest;
import in.lekhai.core.repository.CategoryMasterRepo;
import in.lekhai.core.repository.RoleCategoryMasterRepo;
import in.lekhai.core.repository.TenantDetailsRepo;
import in.lekhai.core.util.AdminUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SuperAdminService {

    private final CategoryMasterRepo categoryMasterRepo;
    private final RoleCategoryMasterRepo roleCategoryMasterRepo;
    private final UserCredentialRepository userCredentialRepository;
    private final TenantDetailsRepo tenantDetailsRepo;
    private final PasswordEncoder passwordEncoder;

    public SuperAdminService(
            CategoryMasterRepo categoryMasterRepo,
            RoleCategoryMasterRepo roleCategoryMasterRepo,
            UserCredentialRepository userCredentialRepository,
            TenantDetailsRepo tenantDetailsRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.categoryMasterRepo = categoryMasterRepo;
        this.roleCategoryMasterRepo = roleCategoryMasterRepo;
        this.userCredentialRepository = userCredentialRepository;
        this.tenantDetailsRepo = tenantDetailsRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AdminRegistrationResponse registerAdmin(AdminRegistrationRequest request) {
        CategoryMaster category = categoryMasterRepo.findByCategory(request.category())
                .orElseThrow(() -> {
                    log.error("Category : {} doesn't exists. failed to create admin {}", request.category(), request.username());
                    return new IllegalArgumentException(String.format("Category %s doesn't exists", request.category()));
                });
        //TODO : change to Role enum
        RoleCategoryMaster roleCategoryMaster = roleCategoryMasterRepo.findByRoleAndCategoryId(category.getId(), "ADMIN")
                .orElseGet(() -> {
                    RoleCategoryMaster adminEntryForCategory = RoleCategoryMaster.builder()
                            .categoryId(category.getId())
                            .role("ADMIN")
                            .permission(category.getPermission())
                            .build();
                    return roleCategoryMasterRepo.save(adminEntryForCategory);
                });

        UserCredentials userCredentials = UserCredentials.builder()
                .uuid(AdminUtils.createUUID(roleCategoryMaster.getRole()))
                .username(request.username())
                .passHash(passwordEncoder.encode(request.password()))
                .build();

        userCredentialRepository.save(userCredentials);
        TenantDetails tenantDetails = TenantDetails.builder()
                .firmName(request.firmName())
                .role(roleCategoryMaster.getRole())
                .uuid(userCredentials.getUuid())
                .category_id(category.getId())
                .gstIn(request.gstIn())
                .permissionBit(category.getPermission())
                .tenant(AdminUtils.createTenant())
                .build();
        tenantDetailsRepo.save(tenantDetails);

        return new AdminRegistrationResponse(
                userCredentials.getUsername(),
                tenantDetails.getFirmName(),
                category.getCategory(),
                tenantDetails.getGstIn(),
                tenantDetails.getTenant()
        );
    }

    public void createCategory(CategoryCreationRequest request) {
        CategoryMaster toBeSavedCategory = CategoryMaster.builder()
                .category(request.categoryName())
                .build();
        categoryMasterRepo.save(toBeSavedCategory);
    }
}
