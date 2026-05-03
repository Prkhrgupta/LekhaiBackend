package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

public record TaxProErrorResponse(
        String status_cd,
        Error error
) {
    public record Error(String error_cd, String message) {}
}