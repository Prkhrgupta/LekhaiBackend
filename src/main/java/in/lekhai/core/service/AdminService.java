package in.lekhai.core.service;

import in.lekhai.authentication.entity.UserCredentials;
import in.lekhai.core.entity.CategoryMaster;
import in.lekhai.core.entity.FeatureMaster;
import in.lekhai.core.entity.RoleCategoryMaster;
import in.lekhai.core.entity.TenantDetails;
import in.lekhai.core.model.enums.Roles;
import in.lekhai.core.model.menu.MenuItem;
import in.lekhai.core.model.menu.MenuResponse;
import in.lekhai.core.repository.CategoryMasterRepo;
import in.lekhai.core.repository.FeatureMasterRepo;
import in.lekhai.core.repository.RoleCategoryMasterRepo;
import in.lekhai.core.repository.TenantDetailsRepo;
import in.lekhai.core.util.CollectionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AdminService {

    private final TenantDetailsRepo tenantDetailsRepo;
    private final CategoryMasterRepo categoryMasterRepo;
    private final RoleCategoryMasterRepo roleCategoryMasterRepo;
    private final FeatureMasterRepo featureMasterRepo;
    private final JwtDecoder jwtDecoder;

    /*
            1. fetch permissions bits for category
            2. permission bits for that role in that category
            3. permission bits of that user
     */
    public MenuResponse generateUiJson() {
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println(principal.getClaims().get("userUuid"));
//        UserCredentials userCredentials = (UserCredentials) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String uuid = principal.getClaims().get("userUuid").toString();
        //TODO: This user can be someone other than a Tenant (ADMIN) too, implement that

        TenantDetails tenantDetails = tenantDetailsRepo.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.error("No entry in tenant details for uuid : {}", uuid);
                    return new RuntimeException("No uuid in tenant details, Entry missing");
                });

        Long categoryId = tenantDetails.getCategory_id();
        Roles role = tenantDetails.getRole();
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
        List<Long> userPermissionBits = tenantDetails.getPermissionBit(); // TODO: userDetails will be used for other Roles, other than ADMIN

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

        return createUiJsonFromFeature(enabledRootFeatures);
    }

    private MenuResponse createUiJsonFromFeature(List<FeatureMaster> leafFeatures) {
        Set<Long> parentIdsToFetch = leafFeatures.stream()
                .map(FeatureMaster::getParentFeatureId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, FeatureMaster> featureMap = leafFeatures.stream()
                .collect(Collectors.toMap(FeatureMaster::getId, f -> f));

        // Recursively fetch all parents until we reach root
        while (!parentIdsToFetch.isEmpty()) {
            List<FeatureMaster> parents = featureMasterRepo.findAllById(parentIdsToFetch);

            parentIdsToFetch.clear();
            for (FeatureMaster parent : parents) {
                if (!featureMap.containsKey(parent.getId())) {
                    featureMap.put(parent.getId(), parent);
                    if (parent.getParentFeatureId() != null) {
                        parentIdsToFetch.add(parent.getParentFeatureId());
                    }
                }
            }
        }

        // Step 2: Now build the hierarchy with all features
        List<FeatureMaster> allFeatures = new ArrayList<>(featureMap.values());

        Map<Long, List<FeatureMaster>> childrenMap = allFeatures.stream()
                .filter(f -> f.getParentFeatureId() != null)
                .collect(Collectors.groupingBy(FeatureMaster::getParentFeatureId));

        List<FeatureMaster> roots = allFeatures.stream()
                .filter(f -> f.getParentFeatureId() == null)
                .sorted(Comparator.comparing(f -> f.getSortOrder() != null ? f.getSortOrder() : Long.MAX_VALUE))
                .toList();

        List<MenuItem> mainMenuItems = roots.stream()
                .map(feature -> buildMenuItem(feature, childrenMap, featureMap))
                .toList();

        return new MenuResponse(mainMenuItems, List.of());
    }
    private MenuItem buildMenuItem(FeatureMaster feature,
                                   Map<Long, List<FeatureMaster>> childrenMap,
                                   Map<Long, FeatureMaster> featureMap) {
        // Get children of current feature, sorted by sortOrder (handling nulls)
        List<FeatureMaster> children = childrenMap.getOrDefault(feature.getId(), List.of())
                .stream()
                .sorted(Comparator.comparing(f -> f.getSortOrder() != null ? f.getSortOrder() : Long.MAX_VALUE))
                .toList();

        if (children.isEmpty()) {
            // Leaf node - construct route
            String route = buildRoute(feature, featureMap);
            return new MenuItem(
                    feature.getFeatureKey(),
                    feature.getTitle(),
                    feature.getIcon(),
                    route,
                    null
            );
        } else {
            // Parent node - recursively build children
            List<MenuItem> childMenuItems = children.stream()
                    .map(child -> buildMenuItem(child, childrenMap, featureMap))
                    .toList();

            return new MenuItem(
                    feature.getFeatureKey(),
                    feature.getTitle(),
                    feature.getIcon(),
                    null,
                    childMenuItems
            );
        }
    }

    private String buildRoute(FeatureMaster feature, Map<Long, FeatureMaster> featureMap) {
        List<String> pathSegments = new ArrayList<>();
        FeatureMaster current = feature;

        // Traverse from leaf to root, collecting feature keys
        while (current != null) {
            pathSegments.add(current.getFeatureKey());
            current = current.getParentFeatureId() != null
                    ? featureMap.get(current.getParentFeatureId())
                    : null;
        }

        // Reverse to get root-to-leaf order
        Collections.reverse(pathSegments);

        // Join with "/" to create route
        return "/" + String.join("/", pathSegments);
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
