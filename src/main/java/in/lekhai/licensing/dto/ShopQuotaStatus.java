package in.lekhai.licensing.dto;

public record ShopQuotaStatus(
        Long shopId,
        Integer shopCode,
        String firmName,
        String gstNumber,
        Integer maxUsers,
        Long currentUsers,
        Boolean canAddUsers
) {
}
