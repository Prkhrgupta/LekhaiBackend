package in.lekhai.core.service;

import in.lekhai.core.entity.*;
import in.lekhai.core.model.enums.Roles;
import in.lekhai.core.model.menu.MenuResponse;
import in.lekhai.core.repository.*;
import in.lekhai.core.util.CollectionUtils;
import in.lekhai.core.util.JwtUtil;
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

        CategoryMaster categoryMaster = categoryMasterRepo.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Something is wrong, user {} have a category that doesn't exists", uuid);
                    return new RuntimeException("User : {} have a categoryId that isn't created / exists");
                });

        RoleCategoryMaster roleCategoryMaster = roleCategoryMasterRepo.findByRoleAndCategoryId(categoryId, role)
                .orElseThrow(() -> {
                    log.error("Something is wrong, user {} role and categoryId that doesn't exist", uuid);
                    return new RuntimeException("User : {} have a role and categoryId that isn't created / exists");
                });


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
                    )); // TODO: create custom or catch globally
            case ADMIN -> tenantDetailsRepo.findByUuid(uuid)
                    .map(tenant -> (BaseUserEntity) tenant)
                    .orElseThrow(() -> {
                        log.error("No tenant entry found for ADMIN role with uuid: {}", uuid);
                        return new RuntimeException(
                                String.format("No tenant found for uuid: %s", uuid)
                        );
                    });
            default -> userDetailsRepo.findByUuid(uuid)
                    .map(user -> (BaseUserEntity) user)
                    .orElseThrow(() -> {
                        log.error("No user entry found for role {} with uuid: {}", role, uuid);
                        return new RuntimeException(
                                String.format("No user found for role %s with uuid: %s", role, uuid)
                        );
                    });
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
