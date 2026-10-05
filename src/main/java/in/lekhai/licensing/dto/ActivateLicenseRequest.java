package in.lekhai.licensing.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivateLicenseRequest(
        @NotBlank(message = "License data is required")
        String licenseData,
        @NotBlank(message = "Signature is required")
        String signature,
        @NotBlank(message = "Admin password is required")
        String adminPassword,
        String adminName
) {
}
