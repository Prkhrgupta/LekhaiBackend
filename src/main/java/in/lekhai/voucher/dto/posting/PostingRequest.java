package in.lekhai.voucher.dto.posting;

import in.lekhai.voucher.entity.VoucherType;

import java.time.LocalDate;
import java.util.List;

public record PostingRequest(
        VoucherType voucherType,
        LocalDate voucherDate,
        String narration,
        List<PostingEntry> entries,
        String seriesCode,
        Long manualBillNo
) {
    public PostingRequest(
            VoucherType voucherType,
            LocalDate voucherDate,
            String narration,
            List<PostingEntry> entries) {
        this(voucherType, voucherDate, narration, entries, null, null);
    }
}
