package in.lekhai.core.model.request;

import java.util.Set;

public record EnableCategoryWiseFeatures (
        Set<Long> categoryIdList,
        Set<Integer> bitsPositionsToBeEnabled
)
{ }
