package in.lekhai.voucher.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.VoucherApi;
import in.lekhai.contract.model.PaymentVoucherRequest;
import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.voucher.service.PaymentVoucherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class VoucherController implements VoucherApi {
    private final PaymentVoucherService paymentVoucherService;

    public VoucherController(PaymentVoucherService paymentVoucherService) {
        this.paymentVoucherService = paymentVoucherService;
    }

    @Override
    public ResponseEntity<VoucherResponse> createPaymentVoucher(
            @Valid PaymentVoucherRequest paymentVoucherRequest
    ) {
        VoucherResponse response = paymentVoucherService.process(paymentVoucherRequest);
        return ResponseEntity.ok(response);
    }
}
