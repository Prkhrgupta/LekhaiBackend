package in.lekhai.core.account_master.dto;

import java.time.Instant;

public record TransportResponse(
        Long id,
        String name,
        String phone,
        String gstNo,
        Instant createdAt
) {
}
