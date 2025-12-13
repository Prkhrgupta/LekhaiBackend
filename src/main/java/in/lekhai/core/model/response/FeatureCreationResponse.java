package in.lekhai.core.model.response;

public record FeatureCreationResponse(
        Long id,
        String featureKey,
        Long bitPosition,
        String title,
        String icon,
        String parentFeatureKey
) { }

