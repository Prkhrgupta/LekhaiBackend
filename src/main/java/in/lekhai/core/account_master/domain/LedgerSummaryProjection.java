package in.lekhai.core.account_master.domain;

import java.math.BigDecimal;

public record LedgerSummaryProjection (
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        BigDecimal currentBalance
){}