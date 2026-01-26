package in.lekhai.core.account_master.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountGroupRequest(
        @NotBlank(message = "name is required") String name,
        Long parentId
) {
}
