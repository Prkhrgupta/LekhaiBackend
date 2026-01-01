package in.lekhai.core.service.admin;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.admin.AdminDetails;
import in.lekhai.core.domain.category.CategoryMaster;
import in.lekhai.core.domain.category.RoleCategoryMaster;
import in.lekhai.core.dto.admin.AdminRegistrationRequest;
import in.lekhai.core.dto.admin.AdminRegistrationResponse;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.admin.AdminDetailsRepo;
import in.lekhai.core.repository.category.CategoryMasterRepo;
import in.lekhai.core.repository.category.RoleCategoryMasterRepo;
import in.lekhai.core.util.AdminUtils;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.error.controller.category.exception.CategoryDoesNotExistException;
import in.lekhai.error.controller.user.exception.UserAlreadyExistException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final CategoryMasterRepo categoryMasterRepo;
    private final RoleCategoryMasterRepo roleCategoryMasterRepo;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AdminDetailsRepo adminDetailsRepo;

    public AdminService(CategoryMasterRepo categoryMasterRepo,
                        RoleCategoryMasterRepo roleCategoryMasterRepo,
                        UserAccountRepository userAccountRepository,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil,
                        AdminDetailsRepo adminDetailsRepo
    ) {
        this.categoryMasterRepo = categoryMasterRepo;
        this.roleCategoryMasterRepo = roleCategoryMasterRepo;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.adminDetailsRepo = adminDetailsRepo;
    }

    @Transactional
    public AdminRegistrationResponse registerAdmin(AdminRegistrationRequest request, Integer tenant) {
        CategoryMaster category = categoryMasterRepo
                .findByCategory(request.category())
                .orElseThrow(() -> new CategoryDoesNotExistException(request.category()));

        userAccountRepository
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

        UserAccounts adminUserAccount = userAccountRepository.save(UserAccounts.builder()
                .uuid(AdminUtils.createUUID(roleCategoryMaster.getRole()))
                .username(request.username())
                .passHash(passwordEncoder.encode(request.password()))
                .build());

        AdminDetails adminToBeRegistered = new AdminDetails();
        adminToBeRegistered.setCategoryId(category.getId());
        adminToBeRegistered.setName(request.name());
        adminToBeRegistered.setTenant(tenant == null ? jwtUtil.extractJwtClaim().tenant() : tenant); // whichever tenant this admin is created from
        adminToBeRegistered.setPermissionBit(category.getPermission());
        adminToBeRegistered.setUuid(adminUserAccount.getUuid());

        AdminDetails savedAdminDetails = adminDetailsRepo.save(adminToBeRegistered);

        return new AdminRegistrationResponse(
                adminUserAccount.getUsername(),
                category.getCategory(),
                savedAdminDetails.getTenant(),
                savedAdminDetails.getUuid(),
                savedAdminDetails.getCategoryId()
        );
    }

    public String changeCurrentTenant(Integer newTenant) {
        return "NEW_JWT_TOKEN";
    }
}
