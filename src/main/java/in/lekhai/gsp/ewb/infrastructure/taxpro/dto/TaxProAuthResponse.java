package in.lekhai.gsp.ewb.infrastructure.taxpro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TaxProAuthResponse(
        @JsonProperty("Status")
        int status,
        @JsonProperty("Data")
        Data data
) {
    public record Data(
            @JsonProperty("AuthToken")
            String authToken,
            @JsonProperty("TokenExpiry")
            String tokenExpiry
    ){}
}
