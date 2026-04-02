package in.lekhai.core.dto.shop;

public record CreateShopExistingAdminRequest(
        String firmName,
        // TODO: Add regex and checkSum validation
        String gstIn,
        String address,
        Boolean isDefault,
        String category,
        String adminUuid
) implements BaseShopRequest { }
