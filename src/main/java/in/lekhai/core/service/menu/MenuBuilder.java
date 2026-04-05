package in.lekhai.core.service.menu;

import in.lekhai.contract.model.MenuItem;
import in.lekhai.contract.model.MenuResponse;
import in.lekhai.core.domain.feature.Features;
import in.lekhai.core.repository.feature.FeaturesRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MenuBuilder {

    private static final Logger log = LoggerFactory.getLogger(MenuBuilder.class);

    private final FeaturesRepo featuresRepo;

    public MenuBuilder(FeaturesRepo featuresRepo) {
        this.featuresRepo = featuresRepo;
    }

    public MenuResponse buildMenu(List<Features> enabledLeafFeatures) {
        Map<Long, Features> allFeatures = fetchAllFeaturesWithParents(enabledLeafFeatures);
        Map<Long, List<Features>> childrenByParentId = buildChildrenMap(allFeatures);
        List<Features> rootFeatures = getRootFeatures(allFeatures);

        List<MenuItem> menuItems = rootFeatures.stream()
                .map(root -> buildMenuItem(root, childrenByParentId, allFeatures))
                .toList();

        return new MenuResponse()
                .mainMenu(menuItems)
                .favourites(List.of());
    }

    private Map<Long, Features> fetchAllFeaturesWithParents(List<Features> initialFeatures) {
        Map<Long, Features> featureMap = initialFeatures.stream()
                .collect(Collectors.toMap(Features::getId, f -> f));

        Set<Long> parentIdsToFetch = extractParentIds(initialFeatures);

        while (!parentIdsToFetch.isEmpty()) {
            List<Features> parentFeatures = featuresRepo.findAllById(parentIdsToFetch);

            parentIdsToFetch.clear();

            for (Features parent : parentFeatures) {
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

    private Set<Long> extractParentIds(List<Features> features) {
        return features.stream()
                .map(Features::getParentFeatureId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Map<Long, List<Features>> buildChildrenMap(Map<Long, Features> allFeatures) {
        return allFeatures.values().stream()
                .filter(feature -> feature.getParentFeatureId() != null)
                .collect(Collectors.groupingBy(Features::getParentFeatureId));
    }

    private List<Features> getRootFeatures(Map<Long, Features> allFeatures) {
        return allFeatures.values().stream()
                .filter(feature -> feature.getParentFeatureId() == null)
                .sorted(Comparator.comparing(f -> f.getDisplayOrder() != null ? f.getDisplayOrder() : Long.MAX_VALUE))
                .toList();
    }

    private MenuItem buildMenuItem(
            Features feature,
            Map<Long, List<Features>> childrenByParentId,
            Map<Long, Features> allFeaturesMap) {
        List<Features> childFeatures = childrenByParentId
                .getOrDefault(feature.getId(), List.of())
                .stream()
                .sorted(Comparator.comparing(f -> f.getDisplayOrder() != null ? f.getDisplayOrder() : Long.MAX_VALUE))
                .toList();

        if (childFeatures.isEmpty()) {
            return createLeafMenuItem(feature);
        } else {
            return createBranchMenuItem(feature, childFeatures, childrenByParentId, allFeaturesMap);
        }
    }

    private MenuItem createLeafMenuItem(Features feature) {
        return new MenuItem()
                .id(feature.getFeatureKey())
                .title(feature.getTitle())
                .icon(feature.getIcon())
                .route(feature.getRoute())
                .children(null);
    }

    private MenuItem createBranchMenuItem(
            Features feature,
            List<Features> childFeatures,
            Map<Long, List<Features>> childrenByParentId,
            Map<Long, Features> allFeaturesMap) {
        List<MenuItem> childMenuItems = childFeatures.stream()
                .map(child -> buildMenuItem(child, childrenByParentId, allFeaturesMap))
                .toList();

        return new MenuItem()
                .id(feature.getFeatureKey())
                .title(feature.getTitle())
                .icon(feature.getIcon())
                .route(null)
                .children(childMenuItems);
    }
}