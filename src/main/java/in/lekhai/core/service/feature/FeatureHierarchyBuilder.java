package in.lekhai.core.service.feature;

import in.lekhai.core.domain.feature.Features;
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
    public List<Features> buildParentHierarchy(
            Features feature,
            Map<Long, Features> featureMap
    ) {
        LinkedList<Features> hierarchy = new LinkedList<>();
        Features current = feature;

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
    public String buildRoute(List<Features> hierarchy) {
        return hierarchy.stream()
                .map(Features::getFeatureKey)
                .collect(Collectors.joining("/", "/", ""));
    }

    /**
     * Gets parent titles in order
     */
    public List<String> getParentTitles(List<Features> hierarchy) {
        return hierarchy.stream()
                .map(Features::getTitle)
                .collect(Collectors.toList());
    }
}