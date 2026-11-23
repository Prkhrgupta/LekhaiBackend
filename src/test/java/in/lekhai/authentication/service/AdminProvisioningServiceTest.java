package in.lekhai.authentication.service;

import in.lekhai.authentication.config.properties.SuperAdminRoutesProperties;
import in.lekhai.authentication.config.properties.SuperAdminRoutesProperties.RouteConfig;
import in.lekhai.authentication.entity.UserAccounts;
import in.lekhai.authentication.repository.UserAccountRepository;
import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.repository.superadmin.SuperAdminDetailsRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.util.PermissionBitCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProvisioningServiceTest {

    private static final List<RouteConfig> ROUTES = List.of(new RouteConfig("/a/b", "B", ""));

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private SuperAdminDetailsRepo superAdminDetailsRepo;
    @Mock
    private CategoriesRepo categoriesRepo;
    @Mock
    private RolePermissionsRepo rolePermissionsRepo;
    @Mock
    private UsersRepo usersRepo;
    @Mock
    private SuperAdminRouteProvisioner routeProvisioner;

    private AdminProvisioningService service;
    private Categories category;

    @BeforeEach
    void setUp() {
        service = new AdminProvisioningService(passwordEncoder, userAccountRepository, superAdminDetailsRepo,
                categoriesRepo, rolePermissionsRepo, usersRepo, new PermissionBitCalculator(), routeProvisioner,
                new SuperAdminRoutesProperties(ROUTES), "admin", "pw", "Admin");
        category = Categories.builder().id(1).name("SUPER_ADMIN").permissions(new ArrayList<>()).build();

        when(categoriesRepo.findByName("SUPER_ADMIN")).thenReturn(Optional.of(category));
        when(userAccountRepository.findByUsername("admin"))
                .thenReturn(Optional.of(UserAccounts.builder().username("admin").uuid("uuid-1").build()));
        when(usersRepo.findByUuid("uuid-1")).thenReturn(Optional.of(Users.createUserWithCategory("uuid-1", "Admin", 1)));
    }

    @Test
    void provisionSuperAdmin_enablesRouteBitsOnCategoryAndNewRole() {
        when(routeProvisioner.ensureRoutes(ROUTES)).thenReturn(Set.of(1, 2));
        when(rolePermissionsRepo.findByCategoryIdAndRoleId(1, Roles.SUPER_ADMIN)).thenReturn(Optional.empty());

        service.provisionSuperAdmin();

        assertThat(category.getPermissions()).containsExactly(6L);
        verify(categoriesRepo).save(category);
        verify(rolePermissionsRepo).save(org.mockito.ArgumentMatchers.argThat(
                role -> role.getPermissions().equals(List.of(6L))));
    }

    @Test
    void provisionSuperAdmin_addsOnlyNewBitsAndKeepsExisting() {
        category.getPermissions().add(2L);
        RolePermissions rolePerm = new RolePermissions(Roles.SUPER_ADMIN, 1, new ArrayList<>(List.of(2L)));
        when(routeProvisioner.ensureRoutes(ROUTES)).thenReturn(Set.of(1, 2));
        when(rolePermissionsRepo.findByCategoryIdAndRoleId(1, Roles.SUPER_ADMIN)).thenReturn(Optional.of(rolePerm));

        service.provisionSuperAdmin();

        assertThat(category.getPermissions()).containsExactly(6L);
        assertThat(rolePerm.getPermissions()).containsExactly(6L);
    }

    @Test
    void provisionSuperAdmin_alreadyGranted_savesNothing() {
        category.getPermissions().add(6L);
        RolePermissions rolePerm = new RolePermissions(Roles.SUPER_ADMIN, 1, new ArrayList<>(List.of(6L)));
        when(routeProvisioner.ensureRoutes(ROUTES)).thenReturn(Set.of(1, 2));
        when(rolePermissionsRepo.findByCategoryIdAndRoleId(1, Roles.SUPER_ADMIN)).thenReturn(Optional.of(rolePerm));

        service.provisionSuperAdmin();

        verify(categoriesRepo, never()).save(any());
        verify(rolePermissionsRepo, never()).save(any());
    }

    @Test
    void provisionSuperAdmin_noRoutes_stillCreatesRoleRowWithoutBits() {
        when(routeProvisioner.ensureRoutes(ROUTES)).thenReturn(Set.of());
        when(rolePermissionsRepo.findByCategoryIdAndRoleId(1, Roles.SUPER_ADMIN)).thenReturn(Optional.empty());

        service.provisionSuperAdmin();

        assertThat(category.getPermissions()).isEmpty();
        verify(rolePermissionsRepo).save(any(RolePermissions.class));
    }
}
