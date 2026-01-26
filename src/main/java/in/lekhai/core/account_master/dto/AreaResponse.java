package in.lekhai.core.account_master.dto;

import java.time.Instant;

public record AreaResponse(
        Long id,
        String areaName,
        String stateCode,
        Integer shopCode,
        Instant createdAt) {
}
