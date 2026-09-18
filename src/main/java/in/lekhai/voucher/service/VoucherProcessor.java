package in.lekhai.voucher.service;

import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.Voucher;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.service.posting.VoucherPostingService;

public abstract class VoucherProcessor<T> {
    protected final VoucherPostingService voucherPostinService;
    protected final VoucherPostingMapper voucherPostingMapper;
    protected VoucherProcessor(
            VoucherPostingService voucherPostinService,
            VoucherPostingMapper voucherPostingMapper
    ) {
        this.voucherPostinService = voucherPostinService;
        this.voucherPostingMapper = voucherPostingMapper;
    }
    abstract void validate(T voucher);
    abstract PostingRequest covertToVoucherPostRequest(T voucher);
    public VoucherResponse process(T voucher) {
        validate(voucher);
        PostingRequest postingRequest = covertToVoucherPostRequest(voucher);
        Voucher createdVoucher = voucherPostinService.post(postingRequest);
        return voucherPostingMapper.mapToCommonVoucherResponse(createdVoucher);
    }
}
