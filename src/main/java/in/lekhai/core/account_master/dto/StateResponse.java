package in.lekhai.core.account_master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StateResponse(
        @JsonProperty("code")
        String stateCode,
        String name) {
}
