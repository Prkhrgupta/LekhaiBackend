package in.lekhai.core.service.menu;

import in.lekhai.core.domain.category.Categories;
import in.lekhai.core.domain.category.RolePermissions;
import in.lekhai.core.domain.feature.Features;
import in.lekhai.core.domain.menu.MenuResponse;
import in.lekhai.core.domain.users.Users;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.category.CategoriesRepo;
import in.lekhai.core.repository.category.RolePermissionsRepo;
import in.lekhai.core.repository.users.UsersRepo;
import in.lekhai.core.service.feature.FeatureMapService;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.core.util.PermissionBitCalculator;
import in.lekhai.error.controller.category.exception.CategoryDoesNotExistException;
import in.lekhai.error.controller.role.exception.RoleForCategoryDoesNotExistException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MenuService {

    private final UsersRepo usersRepo;
    private final CategoriesRepo categoriesRepo;
    private final RolePermissionsRepo rolePermissionsRepo;
    private final MenuBuilder menuBuilder;
    private final PermissionBitCalculator permissionBitCalculator;
    private final FeatureMapService featureMapService;

    public MenuService(UsersRepo usersRepo,
                       CategoriesRepo categoriesRepo,
                       RolePermissionsRepo rolePermissionsRepo,
                       MenuBuilder menuBuilder,
                       PermissionBitCalculator permissionBitCalculator,
                       FeatureMapService featureMapService
    ) {
        this.usersRepo = usersRepo;
        this.categoriesRepo = categoriesRepo;
        this.rolePermissionsRepo = rolePermissionsRepo;
        this.menuBuilder = menuBuilder;
        this.permissionBitCalculator = permissionBitCalculator;
        this.featureMapService = featureMapService;
    }

    public MenuResponse generateMenu() {
        String uuid = JwtUtil.extractJwtClaim().uuid();
        Roles role = JwtUtil.extractJwtClaim().role();

        Users userEntity = usersRepo.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException(
                        String.format("UnException exception, uuid : %s not found. But extracted from JWT", uuid))
                );

        Integer categoryId = userEntity.getCategoryId();

        Categories categories = categoriesRepo
                .findById(categoryId)
                .orElseThrow(() -> new CategoryDoesNotExistException(String.valueOf(categoryId)));

        RolePermissions rolePermissions = rolePermissionsRepo
                .findByCategoryIdAndRoleId(categoryId, role)
                .orElseThrow(() -> new RoleForCategoryDoesNotExistException(role, categoryId));


        List<Long> finalPermissionBits = permissionBitCalculator.calculateFinalPermissions(
                categories.getPermissions(),
                rolePermissions.getPermissions(),
                userEntity.getPermissions(),
                role
        );

        Set<Integer> enabledBitPositions = permissionBitCalculator.extractEnabledBitPositions(finalPermissionBits);
        List<Features> enabledRootFeatures = featureMapService.getFeaturesByBitPositions(enabledBitPositions);

        return menuBuilder.buildMenu(enabledRootFeatures);
    }

//    private Users getUserEntity(String uuid, Role role) {
//        return switch (role) {
//            case SUPER_ADMIN -> throw new RuntimeException(
//                    String.format("Feature map can't be created for %s", Role.SUPER_ADMIN
//                    ));
//            case ADMIN -> adminDetailRepo
//                    .findByUuid(uuid)
//                    .map(admin -> (BaseUserEntity) admin)
//                    .orElseThrow(() -> new TenantDoesNotExistException(uuid, role));
//            default -> userInformationRepo
//                    .findByUuid(uuid)
//                    .map(user -> (BaseUserEntity) user)
//                    .orElseThrow(() -> new UserDoesNotExistException(uuid, role));
//        };
//    }
}
