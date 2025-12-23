package in.lekhai.core.service;

import in.lekhai.core.entity.FeatureMaster;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FeatureHierarchyBuilder {

    /**
     * Builds parent-to-child hierarchy from a feature
     */
    public List<FeatureMaster> buildParentHierarchy(
            FeatureMaster feature,
            Map<Long, FeatureMaster> featureMap
    ) {
        LinkedList<FeatureMaster> hierarchy = new LinkedList<>();
        FeatureMaster current = feature;

        while (current != null) {
            hierarchy.addFirst(current);
            current = current.getParentFeatureId() != null
                    ? featureMap.get(current.getParentFeatureId())
                    : null;
        }
        return hierarchy;
    }

    /**
     * Builds route from hierarchy
     */
    public String buildRoute(List<FeatureMaster> hierarchy) {
        return hierarchy.stream()
                .map(FeatureMaster::getFeatureKey)
                .collect(Collectors.joining("/", "/", ""));
    }

    /**
     * Gets parent titles in order
     */
    public List<String> getParentTitles(List<FeatureMaster> hierarchy) {
        return hierarchy.stream()
                .map(FeatureMaster::getTitle)
                .collect(Collectors.toList());
    }
}