package in.lekhai.core.account_master.dto;

import jakarta.validation.constraints.NotBlank;

public record TransportRequest(
        @NotBlank(message = "name is required") String name,
        String phone,
        String gstNo,
        Integer shopCode) {
}
