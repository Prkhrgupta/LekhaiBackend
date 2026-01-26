package in.lekhai.core.account_master.dto;

import java.time.LocalDateTime;

public record BrokerResponse(
        Long id,
        String name,
        String phone,
        Integer shopCode,
        LocalDateTime createdAt) {
}
