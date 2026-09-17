package in.lekhai.voucher.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.VoucherApi;
import in.lekhai.contract.model.ContraVoucherRequest;
import in.lekhai.contract.model.JournalVoucherRequest;
import in.lekhai.contract.model.PaymentVoucherRequest;
import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.voucher.service.VoucherIntakeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class VoucherController implements VoucherApi {
    private final VoucherIntakeService voucherIntakeService;

    public VoucherController(
            VoucherIntakeService voucherIntakeService
    ) {
        this.voucherIntakeService = voucherIntakeService;
    }

    @Override
    public ResponseEntity<VoucherResponse> createPaymentVoucher(
            @Valid PaymentVoucherRequest paymentVoucherRequest
    ) {
        VoucherResponse response = voucherIntakeService.processPayment(paymentVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoucherResponse> createReceiptVoucher(
            @Valid ReceiptVoucherRequest receiptVoucherRequest
    ) {
        VoucherResponse response = voucherIntakeService.processReceipt(receiptVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoucherResponse> createContraVoucher(
            @Valid ContraVoucherRequest contraVoucherRequest
    ) {
        VoucherResponse response = voucherIntakeService.processContra(contraVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoucherResponse> createJournalVoucher(
            @Valid JournalVoucherRequest journalVoucherRequest
    ) {
        VoucherResponse response = voucherIntakeService.processJournal(journalVoucherRequest);
        return ResponseEntity.ok(response);
    }
}
