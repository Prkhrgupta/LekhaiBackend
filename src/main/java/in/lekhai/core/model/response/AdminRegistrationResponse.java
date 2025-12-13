package in.lekhai.core.model.response;

public record AdminRegistrationResponse(
        String username,
        String firmName,
        String category,
        String gstIn,
        Integer tenant
) {
}
