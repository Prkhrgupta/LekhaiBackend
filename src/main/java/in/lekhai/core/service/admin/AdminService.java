package in.lekhai.core.service.admin;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.dto.admin.AdminRegistrationRequest;
import in.lekhai.core.dto.admin.AdminRegistrationResponse;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
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

        public AdminService(CategoriesRepo categoriesRepo,
                        RolePermissionsRepo rolePermissionsRepo,
                        UserAccountRepository userAccountRepository,
                        PasswordEncoder passwordEncoder,
                        UsersRepo userRepo) {
                this.categoriesRepo = categoriesRepo;
                this.rolePermissionsRepo = rolePermissionsRepo;
                this.userAccountRepository = userAccountRepository;
                this.passwordEncoder = passwordEncoder;
                this.userRepo = userRepo;
        }

        @Transactional
        public AdminRegistrationResponse registerAdmin(AdminRegistrationRequest request, Integer shopCode) {
                Categories category = categoriesRepo
                                .findByName(request.category())
                                .orElseThrow(() -> new CategoryDoesNotExistException(request.category()));

                userAccountRepository
                                .findByUsername(request.username())
                                .ifPresent(user -> {
                                        throw new UserAlreadyExistException(request.username());
                                });

                UserAccounts adminUserAccount = userAccountRepository.save(UserAccounts.builder()
                                .uuid(AdminUtils.createUUID(Roles.ADMIN))
                                .username(request.username())
                                .passHash(passwordEncoder.encode(request.password()))
                                .build());

                Users adminToBeRegistered = new Users();
                adminToBeRegistered.setCategoryId(category.getId());
                adminToBeRegistered.setFullName(request.name());
                adminToBeRegistered.setUuid(adminUserAccount.getUuid());

                Users savedAdminDetails = userRepo.save(adminToBeRegistered);

                return new AdminRegistrationResponse(
                                adminUserAccount.getUsername(),
                                category.getName(),
                                shopCode == null ? JwtUtil.extractJwtClaim().shopCode() : shopCode,
                                savedAdminDetails.getUuid(),
                                savedAdminDetails.getCategoryId());
        }

        public String changeCurrentTenant(Integer newTenant) {
                return "NEW_JWT_TOKEN";
        }
}
