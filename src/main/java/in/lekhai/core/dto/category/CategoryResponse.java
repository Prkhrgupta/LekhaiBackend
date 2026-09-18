package in.lekhai.core.dto.category;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponse(
        Integer id,
        String name,
        List<Long> permissions,
        LocalDateTime createdAt
) {
}
