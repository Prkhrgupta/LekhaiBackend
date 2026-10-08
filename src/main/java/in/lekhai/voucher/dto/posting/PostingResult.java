package in.lekhai.voucher.dto.posting;

import in.lekhai.voucher.entity.Voucher;

public record PostingResult(
        Voucher voucher,
        String displayVoucherNumber,
        Long firstMissingBillNo
) {
}
