package in.lekhai.core.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRegistrationRequest(
        @NotBlank(message = "invalid user name")
        @Size(min = 4)
        String username,
        @NotBlank(message = "invalid password")
        String password,
        String category,
        String name
) {
}
