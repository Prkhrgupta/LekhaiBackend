package in.lekhai.core.account_master.dto;

import java.time.LocalDateTime;

public record StateResponse(
        String stateCode,
        String stateName,
        String gstCode,
        String type,
        LocalDateTime createdAt) {
}
