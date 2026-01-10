package in.lekhai.core.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record AdminRegistrationResponse(
        String username,
        String category,
        Integer tenant,
        @JsonIgnore
        String uuid,
        @JsonIgnore
        Integer categoryId
) { }
