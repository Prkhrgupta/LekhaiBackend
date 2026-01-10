package in.lekhai.core.dto.feature;

public record FeatureCreationResponse(
        Long id,
        String featureKey,
        Integer bitPosition,
        String title,
        String icon,
        String route
) { }

