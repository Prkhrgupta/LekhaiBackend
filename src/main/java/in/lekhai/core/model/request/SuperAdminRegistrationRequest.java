package in.lekhai.core.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SuperAdminRegistrationRequest(
        @NotNull @NotBlank String username,
        @NotNull @NotBlank String password
) {
}
