package in.lekhai.core.service.menu;

import in.lekhai.core.domain.category.CategoryMaster;
import in.lekhai.core.domain.category.RoleCategoryMaster;
import in.lekhai.core.domain.feature.FeatureMaster;
import in.lekhai.core.domain.menu.MenuResponse;
import in.lekhai.core.dto.admin.BaseUserEntity;
import in.lekhai.core.enums.Roles;
import in.lekhai.core.repository.admin.AdminDetailsRepo;
import in.lekhai.core.repository.admin.UserInformationRepo;
import in.lekhai.core.repository.category.CategoryMasterRepo;
import in.lekhai.core.repository.category.RoleCategoryMasterRepo;
import in.lekhai.core.service.feature.FeatureMapService;
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

    private final AdminDetailsRepo adminDetailRepo;
    private final UserInformationRepo userInformationRepo;
    private final CategoryMasterRepo categoryMasterRepo;
    private final RoleCategoryMasterRepo roleCategoryMasterRepo;
    private final JwtUtil jwtUtil;
    private final MenuBuilder menuBuilder;
    private final PermissionBitCalculator permissionBitCalculator;
    private final FeatureMapService featureMapService;

    public MenuService(AdminDetailsRepo adminDetailRepo,
                       UserInformationRepo userInformationRepo,
                       CategoryMasterRepo categoryMasterRepo,
                       RoleCategoryMasterRepo roleCategoryMasterRepo,
                       JwtUtil jwtUtil,
                       MenuBuilder menuBuilder,
                       PermissionBitCalculator permissionBitCalculator,
                       FeatureMapService featureMapService
    ) {
        this.adminDetailRepo = adminDetailRepo;
        this.userInformationRepo = userInformationRepo;
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
            case ADMIN -> adminDetailRepo
                    .findByUuid(uuid)
                    .map(admin -> (BaseUserEntity) admin)
                    .orElseThrow(() -> new TenantDoesNotExistException(uuid, role));
            default -> userInformationRepo
                    .findByUuid(uuid)
                    .map(user -> (BaseUserEntity) user)
                    .orElseThrow(() -> new UserDoesNotExistException(uuid, role));
        };
    }
}
