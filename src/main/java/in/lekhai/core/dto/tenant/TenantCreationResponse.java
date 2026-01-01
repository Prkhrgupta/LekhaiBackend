package in.lekhai.core.dto.tenant;

public record TenantCreationResponse(
        Integer tenant,
        String firmName,
        String gstIn,
        String address
) {
}
