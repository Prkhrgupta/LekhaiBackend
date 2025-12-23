package in.lekhai.core.service;

import in.lekhai.core.entity.BaseUserEntity;
import in.lekhai.core.entity.CategoryMaster;
import in.lekhai.core.entity.FeatureMaster;
import in.lekhai.core.entity.RoleCategoryMaster;
import in.lekhai.core.model.enums.Roles;
import in.lekhai.core.model.menu.MenuResponse;
import in.lekhai.core.repository.CategoryMasterRepo;
import in.lekhai.core.repository.RoleCategoryMasterRepo;
import in.lekhai.core.repository.TenantDetailsRepo;
import in.lekhai.core.repository.UserDetailsRepo;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.core.util.PermissionBitCalculator;
import in.lekhai.error.controller.category.exception.CategoryDoesNotExistException;
import in.lekhai.error.controller.role.exception.RoleForCategoryDoesNotExistException;
import in.lekhai.error.controller.tenant.exception.TenantDoesNotExistException;
import in.lekhai.error.controller.user.exception.UserDoesNotExistException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MenuService {

    private final TenantDetailsRepo tenantDetailsRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final CategoryMasterRepo categoryMasterRepo;
    private final RoleCategoryMasterRepo roleCategoryMasterRepo;
    private final JwtUtil jwtUtil;
    private final MenuBuilder menuBuilder;
    private final PermissionBitCalculator permissionBitCalculator;
    private final FeatureMapService featureMapService;

    public MenuService(TenantDetailsRepo tenantDetailsRepo,
                       UserDetailsRepo userDetailsRepo,
                       CategoryMasterRepo categoryMasterRepo,
                       RoleCategoryMasterRepo roleCategoryMasterRepo,
                       JwtUtil jwtUtil,
                       MenuBuilder menuBuilder,
                       PermissionBitCalculator permissionBitCalculator,
                       FeatureMapService featureMapService
    ) {
        this.tenantDetailsRepo = tenantDetailsRepo;
        this.userDetailsRepo = userDetailsRepo;
        this.categoryMasterRepo = categoryMasterRepo;
        this.roleCategoryMasterRepo = roleCategoryMasterRepo;
        this.jwtUtil = jwtUtil;
        this.menuBuilder = menuBuilder;
        this.permissionBitCalculator = permissionBitCalculator;
        this.featureMapService = featureMapService;
    }

    public MenuResponse generateMenu() {
        String uuid = jwtUtil.extractJwtClaim().uuid();
        Roles role = jwtUtil.extractJwtClaim().role();

        BaseUserEntity userEntity = getUserEntity(uuid, role);
        Long categoryId = userEntity.getCategoryId();

        CategoryMaster categoryMaster = categoryMasterRepo
                .findById(categoryId)
                .orElseThrow(() -> new CategoryDoesNotExistException(String.valueOf(categoryId)));

        RoleCategoryMaster roleCategoryMaster = roleCategoryMasterRepo
                .findByRoleAndCategoryId(categoryId, role)
                .orElseThrow(() -> new RoleForCategoryDoesNotExistException(role, categoryId));


        List<Long> finalPermissionBits = permissionBitCalculator.calculateFinalPermissions(
                categoryMaster.getPermission(),
                roleCategoryMaster.getPermission(),
                userEntity.getPermissionBit(),
                role
        );

        Set<Integer> enabledBitPositions = permissionBitCalculator.extractEnabledBitPositions(finalPermissionBits);
        List<FeatureMaster> enabledRootFeatures = featureMapService.getFeaturesByBitPositions(enabledBitPositions);

        return menuBuilder.buildMenu(enabledRootFeatures);
    }

    private BaseUserEntity getUserEntity(String uuid, Roles role) {
        return switch (role) {
            case SUPER_ADMIN -> throw new RuntimeException(
                    String.format("Feature map can't be created for %s", Roles.SUPER_ADMIN
                    ));
            case ADMIN -> tenantDetailsRepo
                    .findByUuid(uuid)
                    .map(tenant -> (BaseUserEntity) tenant)
                    .orElseThrow(() -> new TenantDoesNotExistException(uuid, role));
            default -> userDetailsRepo
                    .findByUuid(uuid)
                    .map(user -> (BaseUserEntity) user)
                    .orElseThrow(() -> new UserDoesNotExistException(uuid, role));
        };
    }
}
