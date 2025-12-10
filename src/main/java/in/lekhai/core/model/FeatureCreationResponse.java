package in.lekhai.core.model;

public record FeatureCreationResponse(
        Long id,
        String featureKey,
        Long bitPosition,
        String title,
        String icon,
        String parentFeatureKey
) { }

