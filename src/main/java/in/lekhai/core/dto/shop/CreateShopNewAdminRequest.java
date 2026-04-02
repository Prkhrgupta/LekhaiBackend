package in.lekhai.core.dto.shop;

import in.lekhai.core.dto.admin.AdminRegistrationRequest;

public record CreateShopNewAdminRequest(
        String firmName,
        // TODO: Add regex and checkSum validation
        String gstIn,
        String address,
        Boolean isDefault,
        String category,
        AdminRegistrationRequest admin
) implements BaseShopRequest { }
