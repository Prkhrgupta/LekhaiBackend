package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TaxProErrorResponse(
        @JsonProperty("status_cd")
        String statusCd,
        Error error
) {
    public record Error(
            @JsonProperty("error_cd")
            String errorCd,
            String message
    ) {}
}