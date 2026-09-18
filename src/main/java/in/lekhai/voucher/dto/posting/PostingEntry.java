package in.lekhai.voucher.dto.posting;

import java.math.BigDecimal;

public record PostingEntry(
        Long ledgerId,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        String remarks

) {}