package in.lekhai.core.service.menu;

import in.lekhai.core.domain.feature.FeatureMaster;
import in.lekhai.core.domain.menu.MenuItem;
import in.lekhai.core.domain.menu.MenuResponse;
import in.lekhai.core.repository.feature.FeatureMasterRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MenuBuilder {

    private final FeatureMasterRepo featureMasterRepo;

    public MenuResponse buildMenu(List<FeatureMaster> enabledLeafFeatures) {
        Map<Long, FeatureMaster> allFeatures = fetchAllFeaturesWithParents(enabledLeafFeatures);
        Map<Long, List<FeatureMaster>> childrenByParentId = buildChildrenMap(allFeatures);
        List<FeatureMaster> rootFeatures = getRootFeatures(allFeatures);

        List<MenuItem> menuItems = rootFeatures.stream()
                .map(root -> buildMenuItem(root, childrenByParentId, allFeatures))
                .toList();

        return new MenuResponse(menuItems, List.of());
    }

    private Map<Long, FeatureMaster> fetchAllFeaturesWithParents(List<FeatureMaster> initialFeatures) {
        Map<Long, FeatureMaster> featureMap = initialFeatures.stream()
                .collect(Collectors.toMap(FeatureMaster::getId, f -> f));

        Set<Long> parentIdsToFetch = extractParentIds(initialFeatures);

        while (!parentIdsToFetch.isEmpty()) {
            List<FeatureMaster> parentFeatures = featureMasterRepo.findAllById(parentIdsToFetch);

            parentIdsToFetch.clear();

            for (FeatureMaster parent : parentFeatures) {
                if (!featureMap.containsKey(parent.getId())) {
                    featureMap.put(parent.getId(), parent);

                    if (parent.getParentFeatureId() != null) {
                        parentIdsToFetch.add(parent.getParentFeatureId());
                    }
                }
            }
        }

        log.debug("Fetched {} total features including parents", featureMap.size());
        return featureMap;
    }

    private Set<Long> extractParentIds(List<FeatureMaster> features) {
        return features.stream()
                .map(FeatureMaster::getParentFeatureId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Map<Long, List<FeatureMaster>> buildChildrenMap(Map<Long, FeatureMaster> allFeatures) {
        return allFeatures.values().stream()
                .filter(feature -> feature.getParentFeatureId() != null)
                .collect(Collectors.groupingBy(FeatureMaster::getParentFeatureId));
    }

    private List<FeatureMaster> getRootFeatures(Map<Long, FeatureMaster> allFeatures) {
        return allFeatures.values().stream()
                .filter(feature -> feature.getParentFeatureId() == null)
                .sorted(Comparator.comparing(f -> f.getSortOrder() != null ? f.getSortOrder() : Long.MAX_VALUE))
                .toList();
    }

    private MenuItem buildMenuItem(
            FeatureMaster feature,
            Map<Long, List<FeatureMaster>> childrenByParentId,
            Map<Long, FeatureMaster> allFeaturesMap
    ) {
        List<FeatureMaster> childFeatures = childrenByParentId
                .getOrDefault(feature.getId(), List.of())
                .stream()
                .sorted(Comparator.comparing(f -> f.getSortOrder() != null ? f.getSortOrder() : Long.MAX_VALUE))
                .toList();

        if (childFeatures.isEmpty()) {
            return createLeafMenuItem(feature);
        } else {
            return createBranchMenuItem(feature, childFeatures, childrenByParentId, allFeaturesMap);
        }
    }

    private MenuItem createLeafMenuItem(FeatureMaster feature) {
        return new MenuItem(
                feature.getFeatureKey(),
                feature.getTitle(),
                feature.getIcon(),
                feature.getRoute(),
                null
        );
    }

    private MenuItem createBranchMenuItem(
            FeatureMaster feature,
            List<FeatureMaster> childFeatures,
            Map<Long, List<FeatureMaster>> childrenByParentId,
            Map<Long, FeatureMaster> allFeaturesMap
    ) {
        List<MenuItem> childMenuItems = childFeatures.stream()
                .map(child -> buildMenuItem(child, childrenByParentId, allFeaturesMap))
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