package in.lekhai.core.dto.category;

import java.time.LocalDateTime;

public record CategoryResponse(
        Integer id,
        String name,
        LocalDateTime createdAt
) {
}
