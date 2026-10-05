package in.lekhai.licensing.domain;

public record ShopLicenseProfile(
        Integer shopCode,
        String firmName,
        String gstNumber,
        String registeredAddress,
        String categoryName,
        Integer maxUsers
) {
}
