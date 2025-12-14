package in.lekhai.core.model.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SuperAdminRegistrationResponse(
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String username
) {
}
