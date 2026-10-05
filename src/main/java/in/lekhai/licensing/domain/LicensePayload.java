package in.lekhai.licensing.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record LicensePayload(
        String licenseId,
        String issuedTo,
        String adminUsername,
        LocalDateTime issuedAt,
        LocalDateTime expiresAt,
        List<ShopLicenseProfile> shops,
        Map<String, Boolean> features
) {
    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
