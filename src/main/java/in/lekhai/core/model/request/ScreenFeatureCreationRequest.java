package in.lekhai.core.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScreenFeatureCreationRequest (
        @NotNull String featureKey,
        Long parentId,
        @NotNull @NotBlank String title,
        String icon,
        @NotNull Boolean isScreen
) {
}
