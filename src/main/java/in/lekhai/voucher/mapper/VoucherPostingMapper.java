package in.lekhai.voucher.mapper;

import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.entity.VoucherEntry;
import org.springframework.stereotype.Component;

@Component
public class VoucherPostingMapper {
    public Voucher mapToVoucher(PostingRequest request, Long voucherNo) {
        return new Voucher(
                request.voucherType(),
                voucherNo,
                request.voucherDate(),
                request.narration()
        );
    }

    public VoucherEntry mapToVoucherEntry(
            PostingEntry entry,
            Long voucherId,
            Integer lineNumber
    ) {
        return new VoucherEntry(
                voucherId,
                entry.ledgerId(),
                lineNumber,
                entry.debitAmount(),
                entry.creditAmount(),
                entry.remarks()
        );
    }

    public VoucherResponse mapToCommonVoucherResponse(Voucher voucher) {
        String displayVoucherNo = voucher.getVoucherType().getPrefix() + "-" + voucher.getVoucherNumber().toString();
        return new VoucherResponse()
                .id(voucher.getId())
                .voucherNumber(displayVoucherNo);
    }
}
