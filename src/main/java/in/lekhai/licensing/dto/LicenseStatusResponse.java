package in.lekhai.licensing.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record LicenseStatusResponse(
        Boolean isLicensed,
        String licenseId,
        String licenseeName,
        String adminUsername,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        Long daysRemaining,
        Boolean isExpired,
        Boolean isReadOnlyMode,
        List<ShopQuotaStatus> shops,
        Map<String, Boolean> features
) {
    public static LicenseStatusResponse unauthenticated() {
        return new LicenseStatusResponse(
                false,
                null,
                null,
                null,
                null,
                null,
                0L,
                true,
                true,
                List.of(),
                Map.of()
        );
    }
}
