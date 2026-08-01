package in.lekhai.voucher.service;

import in.lekhai.contract.model.PaymentVoucherEntry;
import in.lekhai.contract.model.PaymentVoucherRequest;
import in.lekhai.voucher.dto.posting.PostingEntry;
import in.lekhai.voucher.dto.posting.PostingRequest;
import in.lekhai.voucher.entity.VoucherType;
import in.lekhai.voucher.mapper.VoucherPostingMapper;
import in.lekhai.voucher.service.posting.VoucherPostingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentVoucherService extends VoucherProcessor<PaymentVoucherRequest> {
    public PaymentVoucherService(
            VoucherPostingService voucherPostingService,
            VoucherPostingMapper voucherPostingMapper
    ) {
        super(voucherPostingService, voucherPostingMapper);
    }

    /**
     * 1. check is the paymentAccountId (Cr. acc) is in Bank or Cash In Hand Acc grp
     * 2. accountId in paymentVoucherEntry should not be in Bank or Cash (This is the use of Contra)
     * 3. at least 1 paymentEntry
     */
    @Override
    void validate(PaymentVoucherRequest voucher) {
    }

    @Override
    PostingRequest covertToVoucherPostRequest(PaymentVoucherRequest paymentVoucher) {
        List<PostingEntry> postingEntryList = new ArrayList<>();
        BigDecimal totalDebitAmount = BigDecimal.ZERO;

        // Debit entries
        for(PaymentVoucherEntry entry : paymentVoucher.getItems()) {
            PostingEntry postingEntry = new PostingEntry(
                    entry.getAccountId(),
                    entry.getAmount(),
                    BigDecimal.ZERO,
                    entry.getRemarks()
            );
            postingEntryList.add(postingEntry);
            totalDebitAmount = totalDebitAmount.add(entry.getAmount());
        }

        // Credit entry ( only one acc )
        postingEntryList.add(new PostingEntry(
                paymentVoucher.getPaymentAccountId(),
                BigDecimal.ZERO,
                totalDebitAmount,
                paymentVoucher.getNarration()
        ));

        // convert to actual posting request
        return new PostingRequest(
                VoucherType.PAYMENT,
                paymentVoucher.getVoucherDate(),
                paymentVoucher.getNarration(),
                postingEntryList
        );
    }
}
