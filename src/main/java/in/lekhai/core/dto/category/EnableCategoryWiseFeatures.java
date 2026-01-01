package in.lekhai.core.dto.category;

import java.util.Set;

public record EnableCategoryWiseFeatures (
        Set<Long> categoryIdList,
        Set<Integer> bitsPositionsToBeEnabled
)
{ }
