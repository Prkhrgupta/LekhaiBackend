package in.lekhai.core.account_master.dto;

import in.lekhai.core.account_master.domain.Ledger.OpeningBalanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LedgerRequest(
                @NotBlank(message = "name is required") String name,
                String legalName,
                @NotNull(message = "accountGroupId is required") Long accountGroupId,
                BigDecimal openingBalance,
                @NotNull(message = "openingBalanceType is required") OpeningBalanceType openingBalanceType,
                BigDecimal creditLimit,
                Long defaultAreaId,
                Long defaultBrokerId,
                Long defaultTransportId,
                String pan,
                String aadhaar,
                String tan,
                String email,
                String msme,
                Integer shopCode) {
}
