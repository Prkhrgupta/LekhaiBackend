package in.lekhai.core.account_master.dto;

import jakarta.validation.constraints.NotBlank;

public record AreaRequest(
        @NotBlank(message = "area name is required") String areaName,
        @NotBlank(message = "state code is required") String stateCode) {
}
