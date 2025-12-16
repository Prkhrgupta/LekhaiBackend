package in.lekhai.core.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record FeatureResponse(
        Long id,
        String featureKey,
        String title,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer bitPosition,
        Boolean isActive,
        List<String> parentFeaturesOrder,
        String route
) { }
