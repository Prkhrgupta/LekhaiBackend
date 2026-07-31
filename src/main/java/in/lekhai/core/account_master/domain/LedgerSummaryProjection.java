package in.lekhai.core.account_master.domain;

import java.math.BigDecimal;

public interface LedgerSummaryProjection {
    BigDecimal getTotalDebit();
    BigDecimal getTotalCredit();
    BigDecimal getCurrentBalance();
}