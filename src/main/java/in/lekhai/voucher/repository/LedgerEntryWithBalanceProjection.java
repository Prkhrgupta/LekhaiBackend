package in.lekhai.voucher.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerEntryWithBalanceProjection(
        Long id,
        Long voucherId,
        Long ledgerId,
        Integer lineNumber,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        LocalDate voucherDate,
        LocalDate clearDate,
        BigDecimal runningNet
) {
    public LedgerEntryWithBalanceProjection(
            Long id,
            Long voucherId,
            Long ledgerId,
            Integer lineNumber,
            BigDecimal debitAmount,
            BigDecimal creditAmount,
            LocalDate voucherDate,
            BigDecimal runningNet
    ) {
        this(id, voucherId, ledgerId, lineNumber, debitAmount, creditAmount, voucherDate, null, runningNet);
    }
}
