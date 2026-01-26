package in.lekhai.core.account_master.dto;

import java.time.LocalDateTime;

public record AccountGroupResponse(
        Long id,
        String name,
        Long parentId,
        String nature,
        String behaviour,
        Boolean isPrimary,
        Integer shopCode,
        LocalDateTime createdAt) {
}
