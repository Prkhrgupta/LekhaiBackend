package in.lekhai.core.account_master.dto.ledger;

import in.lekhai.core.account_master.domain.Ledger.OpeningBalanceType;

import java.math.BigDecimal;

public record LedgerResponse(
                Long id,
                String name,
                String legalName,
                Long accountGroupId,
                BigDecimal openingBalance,
                OpeningBalanceType openingBalanceType,
                BigDecimal creditLimit,
                Long defaultAreaId,
                Long defaultBrokerId,
                Long defaultTransportId,
                String pan,
                String aadhaar,
                String tan,
                String email,
                String msme
) {
}
