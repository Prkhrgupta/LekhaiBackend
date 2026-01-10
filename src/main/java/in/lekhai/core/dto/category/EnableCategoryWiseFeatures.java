package in.lekhai.core.dto.category;

import java.util.Set;

public record EnableCategoryWiseFeatures (
        Set<Integer> categoryIdList,
        Set<Integer> bitsPositionsToBeEnabled
)
{ }
