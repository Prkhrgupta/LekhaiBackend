package in.lekhai.voucher.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.VoucherApi;
import in.lekhai.contract.model.PaymentVoucherRequest;
import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.voucher.service.PaymentVoucherService;
import in.lekhai.voucher.service.ReceiptVoucherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class VoucherController implements VoucherApi {
    private final PaymentVoucherService paymentVoucherService;
    private final ReceiptVoucherService receiptVoucherService;

    public VoucherController(
            PaymentVoucherService paymentVoucherService,
            ReceiptVoucherService receiptVoucherService
    ) {
        this.paymentVoucherService = paymentVoucherService;
        this.receiptVoucherService = receiptVoucherService;
    }

    @Override
    public ResponseEntity<VoucherResponse> createPaymentVoucher(
            @Valid PaymentVoucherRequest paymentVoucherRequest
    ) {
        VoucherResponse response = paymentVoucherService.process(paymentVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoucherResponse> createReceiptVoucher(
            @Valid ReceiptVoucherRequest receiptVoucherRequest
    ) {
        VoucherResponse response = receiptVoucherService.process(receiptVoucherRequest);
        return ResponseEntity.ok(response);
    }
}
