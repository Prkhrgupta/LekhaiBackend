package in.lekhai.core.dto.superadmin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SuperAdminRegistrationResponse(
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String username
) {
}
