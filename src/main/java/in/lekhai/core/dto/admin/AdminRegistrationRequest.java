package in.lekhai.core.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminRegistrationRequest(
        @NotNull
        @NotBlank
        @Size(min = 4)
        String username,
        @NotNull
        @NotBlank
        String password,
        String category,
        String name
) {
}
