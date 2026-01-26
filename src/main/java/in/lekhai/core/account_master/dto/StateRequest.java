package in.lekhai.core.account_master.dto;

import jakarta.validation.constraints.NotBlank;

public record StateRequest(
        @NotBlank(message = "state code is required") String stateCode,
        @NotBlank(message = "state name is required") String stateName,
        String gstCode,
        String type) {
}
