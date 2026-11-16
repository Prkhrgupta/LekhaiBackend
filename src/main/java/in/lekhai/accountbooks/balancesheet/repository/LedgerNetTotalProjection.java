package in.lekhai.accountbooks.balancesheet.repository;

import java.math.BigDecimal;

public record LedgerNetTotalProjection(
        Long ledgerId,
        BigDecimal totalDebit,
        BigDecimal totalCredit
) {}
