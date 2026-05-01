package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TaxProAuthResponse(
        int status,
        @JsonProperty("authtoken")
        String authToken
) { }
