package in.lekhai.core.account_master.dto;

import java.time.LocalDateTime;

public record TransportResponse(
        Long id,
        String name,
        String phone,
        String gstNo,
        Integer shopCode,
        LocalDateTime createdAt) {
}
