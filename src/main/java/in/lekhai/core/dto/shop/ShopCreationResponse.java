package in.lekhai.core.dto.shop;

public record ShopCreationResponse(
        Integer shopCode,
        String firmName,
        String gstIn,
        String address
) {
}
