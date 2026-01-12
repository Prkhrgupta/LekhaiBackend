package in.lekhai.core.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreationResponse(
        @NotBlank String category
) {
}
