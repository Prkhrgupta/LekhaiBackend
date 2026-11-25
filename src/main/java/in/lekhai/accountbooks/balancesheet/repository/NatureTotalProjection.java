package in.lekhai.accountbooks.balancesheet.repository;

import java.math.BigDecimal;

public record NatureTotalProjection(
        String nature,
        BigDecimal totalDebit,
        BigDecimal totalCredit
) {}
