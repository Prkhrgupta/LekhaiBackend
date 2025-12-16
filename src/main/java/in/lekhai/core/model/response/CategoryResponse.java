package in.lekhai.core.model.response;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        LocalDateTime createdAt
) {
}
