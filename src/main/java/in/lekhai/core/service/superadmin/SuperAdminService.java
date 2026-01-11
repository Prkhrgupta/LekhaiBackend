package in.lekhai.core.service.superadmin;

import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.dto.superadmin.SuperAdminRegistrationRequest;
import in.lekhai.core.dto.superadmin.SuperAdminRegistrationResponse;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.repository.shop.ShopsRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.service.admin.AdminService;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.error.controller.user.exception.UserAlreadyExistException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static in.lekhai.core.util.AdminUtils.createUUID;

@Service
@Slf4j
public class SuperAdminService {

        private final CategoriesRepo categoriesRepo;
        private final RolePermissionsRepo rolePermissionsRepo;
        private final UserAccountRepository userAccountRepository;
        private final ShopsRepo shopsRepo;
        private final PasswordEncoder passwordEncoder;
        private final AdminService adminService;
        private final UsersRepo usersRepo;

        public SuperAdminService(
                        CategoriesRepo categoriesRepo,
                        RolePermissionsRepo rolePermissionsRepo,
                        UserAccountRepository userAccountRepository,
                        ShopsRepo shopsRepo,
                        PasswordEncoder passwordEncoder,
                        AdminService adminService,
                        UsersRepo usersRepo) {
                this.categoriesRepo = categoriesRepo;
                this.rolePermissionsRepo = rolePermissionsRepo;
                this.userAccountRepository = userAccountRepository;
                this.shopsRepo = shopsRepo;
                this.passwordEncoder = passwordEncoder;
                this.adminService = adminService;
                this.usersRepo = usersRepo;
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

                Users superAdminMasterResponse = usersRepo.save(
                                Users.createSuperAdminUser(superAdminRegistrationResponse.getUuid(),
                                                request.name()));

                return new SuperAdminRegistrationResponse(superAdminMasterResponse.getFullName(),
                                superAdminRegistrationResponse.getUsername());
        }
}
