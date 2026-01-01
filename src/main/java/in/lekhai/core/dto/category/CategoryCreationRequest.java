package in.lekhai.core.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryCreationRequest(
        @NotNull @NotBlank String categoryName
) {
}
