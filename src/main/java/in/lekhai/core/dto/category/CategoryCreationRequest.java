package in.lekhai.core.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreationRequest(
        @NotBlank(message = "invalid category name")
        String categoryName
) {
}
