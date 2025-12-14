package in.lekhai.core.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryCreationRequest(
        @NotNull @NotBlank String categoryName
) {
}
