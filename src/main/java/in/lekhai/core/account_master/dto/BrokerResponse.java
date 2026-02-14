package in.lekhai.core.account_master.dto;

import java.time.Instant;

public record BrokerResponse(
        Long id,
        String name,
        String phone,
        Instant createdAt
) {
}
