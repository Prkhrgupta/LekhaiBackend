package in.lekhai.core.model.response;

public record FeatureCreationResponse(
        Long id,
        String featureKey,
        Integer bitPosition,
        String title,
        String icon,
        String route
) { }

