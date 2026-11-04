package in.lekhai.core.dto.shop;

public record CreateShopExistingAdminRequest(
        String firmName,
        // TODO: Add regex and checkSum validation
        String gstIn,
        String address,
        Boolean isDefault,
        // Seller state (state.state_code, e.g. MH); drives intra/inter-state GST on sales invoices
        String stateCode,
        String adminUuid
) implements BaseShopRequest { }
