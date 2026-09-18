package in.lekhai.voucher.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.VoucherApi;
import in.lekhai.contract.model.ContraVoucherRequest;
import in.lekhai.contract.model.JournalVoucherRequest;
import in.lekhai.contract.model.PaymentVoucherRequest;
import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.voucher.service.ContraVoucherService;
import in.lekhai.voucher.service.JournalVoucherService;
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
    private final ContraVoucherService contraVoucherService;
    private final JournalVoucherService journalVoucherService;

    public VoucherController(
            PaymentVoucherService paymentVoucherService,
            ReceiptVoucherService receiptVoucherService,
            ContraVoucherService contraVoucherService,
            JournalVoucherService journalVoucherService
    ) {
        this.paymentVoucherService = paymentVoucherService;
        this.receiptVoucherService = receiptVoucherService;
        this.contraVoucherService = contraVoucherService;
        this.journalVoucherService = journalVoucherService;
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

    @Override
    public ResponseEntity<VoucherResponse> createContraVoucher(
            @Valid ContraVoucherRequest contraVoucherRequest
    ) {
        VoucherResponse response = contraVoucherService.process(contraVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoucherResponse> createJournalVoucher(
            @Valid JournalVoucherRequest journalVoucherRequest
    ) {
        VoucherResponse response = journalVoucherService.process(journalVoucherRequest);
        return ResponseEntity.ok(response);
    }
}
