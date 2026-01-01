package in.lekhai.core.dto.tenant;

public record CreateTenantExistingAdminRequest(
        String firmName,
        // TODO: Add regex and checkSum validation
        String gstIn,
        String address,
        Boolean isDefault,
        String adminUuid
) implements BaseTenantRequest { }
