package in.lekhai.core.service.admin;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.domain.shop.Shops;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.dto.admin.AdminRegistrationRequest;
import in.lekhai.core.dto.admin.AdminRegistrationResponse;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.util.AdminUtils;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.error.controller.category.exception.CategoryDoesNotExistException;
import in.lekhai.error.controller.user.exception.UserAlreadyExistException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final CategoriesRepo categoriesRepo;
    private final RolePermissionsRepo rolePermissionsRepo;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepo userRepo;
    private final ShopsRepo shopsRepo;

    public AdminService(CategoriesRepo categoriesRepo,
                        RolePermissionsRepo rolePermissionsRepo,
                        UserAccountRepository userAccountRepository,
                        PasswordEncoder passwordEncoder,
                        UsersRepo userRepo,
                        ShopsRepo shopsRepo) {
        this.categoriesRepo = categoriesRepo;
        this.rolePermissionsRepo = rolePermissionsRepo;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.shopsRepo = shopsRepo;
    }

    @Transactional
    public AdminRegistrationResponse registerAdmin(AdminRegistrationRequest request, String categoryName, Integer shopCode) {
        Integer resolvedShopCode = shopCode == null ? JwtUtil.extractJwtClaim().shopCode() : shopCode;
        Categories category;
        if (categoryName != null) {
            category = categoriesRepo
                    .findByName(categoryName)
                    .orElseThrow(() -> new CategoryDoesNotExistException(categoryName));
        } else {
            Shops shop = shopsRepo.findByShopCode(resolvedShopCode)
                    .orElseThrow(() -> new IllegalArgumentException("Shop not found for code: " + resolvedShopCode));
            category = categoriesRepo.findById(shop.getCategoryId())
                    .orElseThrow(() -> new CategoryDoesNotExistException(String.valueOf(shop.getCategoryId())));
        }

        userAccountRepository
                .findByUsername(request.username())
                .ifPresent(user -> {
                    throw new UserAlreadyExistException(request.username());
                });

        UserAccounts adminUserAccount = userAccountRepository.save(UserAccounts.builder()
                .uuid(AdminUtils.createUUID(Roles.SHOP_OWNER))
                .username(request.username())
                .passHash(passwordEncoder.encode(request.password()))
                .build());

        rolePermissionsRepo.findByCategoryIdAndRoleId(category.getId(), Roles.SHOP_OWNER)
                .orElseGet(() -> {
                    RolePermissions rolePermissions = new RolePermissions(Roles.SHOP_OWNER, category.getId());
                    return rolePermissionsRepo.save(rolePermissions);
                });

        Users adminToBeRegistered = new Users();
        adminToBeRegistered.setCategoryId(category.getId());
        adminToBeRegistered.setName(request.name());
        adminToBeRegistered.setUuid(adminUserAccount.getUuid());

        Users savedAdminDetails = userRepo.save(adminToBeRegistered);

        return new AdminRegistrationResponse(
                adminUserAccount.getUsername(),
                category.getName(),
                resolvedShopCode,
                savedAdminDetails.getUuid(),
                savedAdminDetails.getCategoryId());
    }
}
