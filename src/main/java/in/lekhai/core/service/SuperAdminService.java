package in.lekhai.core.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.core.entity.SuperAdminMaster;
import in.lekhai.core.model.request.EnableCategoryWiseFeatures;
import in.lekhai.core.model.response.CategoryCreationResponse;
import in.lekhai.core.model.response.CategoryResponse;
import in.lekhai.core.repository.SuperAdminMasterRepo;
import in.lekhai.error.controller.category.exception.CategoryAlreadyExistException;
import in.lekhai.error.controller.category.exception.CategoryDoesNotExistException;
import in.lekhai.error.controller.user.exception.UserAlreadyExistException;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        userCredentialRepository
                .findByUsername(request.username())
                .ifPresent(user -> {
                    throw new UserAlreadyExistException(request.username());
                });

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

        userCredentialRepository
                .findByUsername(request.username())
                .ifPresent(user -> {
                    throw new UserAlreadyExistException(request.username());
                });

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

    public CategoryCreationResponse createCategory(CategoryCreationRequest request) {
        categoryMasterRepo
                .findByCategory(request.categoryName())
                .ifPresent(category -> {
                    throw new CategoryAlreadyExistException(request.categoryName());
                });

        CategoryMaster savedCategoryResponse = categoryMasterRepo.save(CategoryMaster.builder()
                .category(request.categoryName()).build());

        return new CategoryCreationResponse(savedCategoryResponse.getCategory());
    }

    public List<CategoryResponse> listOfCategories() {
        return categoryMasterRepo.findAll()
                .stream()
                .map((categoryMaster) ->
                        new CategoryResponse(categoryMaster.getId(),
                                categoryMaster.getCategory(),
                                categoryMaster.getCreatedAt()
                        ))
                .toList();
    }

    public void enableFeaturesForCategory(EnableCategoryWiseFeatures request) {
        //TODO: check if the bits and category id are valid, send a list of category or permission that don't exists
        List<CategoryMaster> categoryMasterList = categoryMasterRepo.findAllById(request.categoryIdList());
        Map<Long, List<Long>> categoryPermissionMap = categoryMasterList
                .stream()
                .peek(category -> {
                    addOrEnableBits(category.getPermission(), request.bitsPositionsToBeEnabled());
                })
                .collect(Collectors.toMap(
                        CategoryMaster::getId,
                        CategoryMaster::getPermission
                ));

        categoryMasterRepo.saveAll(categoryMasterList);
    }

    private void addOrEnableBits(List<Long> permissions, Set<Integer> bits) {
        for (Integer bitPosition : bits) {
            int index = bitPosition / 64;
            int bit = bitPosition % 64;

            while (permissions.size() <= index) {
                permissions.add(0L);
            }

            Long currentValue = permissions.get(index);
            permissions.set(index, currentValue | (1L << bit));
        }
    }
}
