package in.lekhai.core.service;

import in.lekhai.core.entity.*;
import in.lekhai.core.model.enums.Roles;
import in.lekhai.core.model.menu.MenuResponse;
import in.lekhai.core.repository.*;
import in.lekhai.core.util.CollectionUtils;
import in.lekhai.core.util.JwtUtil;
import in.lekhai.error.controller.category.exception.CategoryDoesNotExistException;
import in.lekhai.error.controller.role.exception.RoleForCategoryDoesNotExistException;
import in.lekhai.error.controller.tenant.exception.TenantDoesNotExistException;
import in.lekhai.error.controller.user.exception.UserDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MenuService {

    private final TenantDetailsRepo tenantDetailsRepo;
    private final UserDetailsRepo userDetailsRepo;
    private final CategoryMasterRepo categoryMasterRepo;
    private final RoleCategoryMasterRepo roleCategoryMasterRepo;
    private final FeatureMasterRepo featureMasterRepo;
    private final JwtUtil jwtUtil;
    private final MenuBuilder menuBuilder;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public MenuService(TenantDetailsRepo tenantDetailsRepo,
                       UserDetailsRepo userDetailsRepo,
                       CategoryMasterRepo categoryMasterRepo,
                       RoleCategoryMasterRepo roleCategoryMasterRepo,
                       FeatureMasterRepo featureMasterRepo,
                       JwtUtil jwtUtil,
                       MenuBuilder menuBuilder
    ) {
        this.tenantDetailsRepo = tenantDetailsRepo;
        this.userDetailsRepo = userDetailsRepo;
        this.categoryMasterRepo = categoryMasterRepo;
        this.roleCategoryMasterRepo = roleCategoryMasterRepo;
        this.featureMasterRepo = featureMasterRepo;
        this.jwtUtil = jwtUtil;
        this.menuBuilder = menuBuilder;
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


        List<Long> categoryPermissionBits = categoryMaster.getPermission();
        List<Long> roleCategoryPermissionBits = roleCategoryMaster.getPermission();
        List<Long> userPermissionBits = userEntity.getPermissionBit();

        List<Long> finalPermissionBits = new ArrayList<>(categoryPermissionBits.size());
        for(int i = 0; i < categoryPermissionBits.size(); i++)  {
            finalPermissionBits.add(
                    CollectionUtils.getOrDefault(categoryPermissionBits, i, Long.MAX_VALUE) &
                    CollectionUtils.getOrDefault(roleCategoryPermissionBits, i, Roles.ADMIN.equals(role) ? Long.MAX_VALUE : 0L) &
                    CollectionUtils.getOrDefault(userPermissionBits, i, Long.MAX_VALUE)
            );
        }

        log.info("Final Permission bit : {}", finalPermissionBits);

        List<FeatureMaster> enabledRootFeatures = getAllEnabledRootFeatures(finalPermissionBits);
        if(enabledRootFeatures.isEmpty()) {
            log.warn("Enabled feature list is empty for : uuid {}", uuid);
        }

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

    private List<FeatureMaster> getAllEnabledRootFeatures(List<Long> permissionBits) {
        Set<Integer> enabledBitSet = new HashSet<>();
        for(int index = 0; index < permissionBits.size(); index++) {
            Long value = permissionBits.get(index);
            int basePosition = index * 64;
            for(int bit = 0; bit < 64; bit++) {
                if((value & (1L << bit)) != 0) {
                    enabledBitSet.add(basePosition + bit);
                }
            }
        }
        return featureMasterRepo.findByBitPositionIn(enabledBitSet);
    }
}
