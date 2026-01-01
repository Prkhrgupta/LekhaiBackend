package in.lekhai.core.dto.tenant;

import in.lekhai.core.dto.admin.AdminRegistrationRequest;

public record CreateTenantNewAdminRequest(
        String firmName,
        // TODO: Add regex and checkSum validation
        String gstIn,
        String address,
        Boolean isDefault,
        AdminRegistrationRequest admin
) implements BaseTenantRequest { }
