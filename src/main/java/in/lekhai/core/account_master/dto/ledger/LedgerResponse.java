package in.lekhai.core.account_master.dto.ledger;

import com.fasterxml.jackson.annotation.JsonProperty;
import in.lekhai.common.AccountEntryType;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerResponse(
        Long id,
        String name,
        @JsonProperty("legal_name") String legalName,
        @JsonProperty("account_group_id") Long accountGroupId,
        @JsonProperty("opening_balance") BigDecimal openingBalance,
        @JsonProperty("opening_balance_type") AccountEntryType openingBalanceType,
        @JsonProperty("credit_limit") BigDecimal creditLimit,
        String pan,
        String aadhaar,
        String tan,
        String email,
        String msme,
        @JsonProperty("is_active") Boolean isActive,
        Area area,
        Broker broker,
        Transport transport,
        @JsonProperty("created_at") Instant createdAt) {
    public record Area(
            Long id,
            String name,
            @JsonProperty("state_code") String stateCode) {
    }

    public record Broker(
            Long id,
            String name,
            String phone) {
    }

    public record Transport(
            Long id,
            String name,
            String phone,
            String gst) {
    }
}
