package in.lekhai.core.dto.superadmin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SuperAdminRegistrationRequest(
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String username,
        @NotNull @NotBlank String password
) {
}
