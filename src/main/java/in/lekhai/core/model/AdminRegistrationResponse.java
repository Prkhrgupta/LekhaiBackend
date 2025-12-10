package in.lekhai.core.model;

public record AdminRegistrationResponse(
        String username,
        String firmName,
        String category,
        String gstIn,
        Integer tenant
) {
}
