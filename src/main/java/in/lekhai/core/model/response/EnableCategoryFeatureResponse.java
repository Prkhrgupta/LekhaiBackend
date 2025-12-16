package in.lekhai.core.model.response;

import in.lekhai.core.model.request.FeatureResponse;

import java.util.List;

public record EnableCategoryFeatureResponse(
        List<String> featuresEnabled
) { }
