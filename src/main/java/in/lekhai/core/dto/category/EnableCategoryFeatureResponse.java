package in.lekhai.core.dto.category;

import java.util.List;

public record EnableCategoryFeatureResponse(
        List<String> featuresEnabled
) { }
