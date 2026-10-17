package in.lekhai.voucher.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.VoucherApi;
import in.lekhai.contract.model.ContraVoucherRequest;
import in.lekhai.contract.model.CreateSeriesRequest;
import in.lekhai.contract.model.CustomVoucherSeries;
import in.lekhai.contract.model.CustomVoucherSeriesListResponse;
import in.lekhai.contract.model.JournalVoucherRequest;
import in.lekhai.contract.model.NextBillNoResponse;
import in.lekhai.contract.model.NextVoucherNumberResponse;
import in.lekhai.contract.model.PaymentVoucherRequest;
import in.lekhai.contract.model.ReceiptVoucherRequest;
import in.lekhai.contract.model.UpdateSeriesRequest;
import in.lekhai.contract.model.VoucherResponse;
import in.lekhai.contract.model.VoucherType;
import in.lekhai.voucher.service.VoucherCounterService;
import in.lekhai.voucher.service.VoucherIntakeService;
import in.lekhai.voucher.service.VoucherSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class VoucherController implements VoucherApi {
    private final VoucherIntakeService voucherIntakeService;
    private final VoucherSeriesService voucherSeriesService;
    private final VoucherCounterService voucherCounterService;

    public VoucherController(
            VoucherIntakeService voucherIntakeService,
            VoucherSeriesService voucherSeriesService,
            VoucherCounterService voucherCounterService
    ) {
        this.voucherIntakeService = voucherIntakeService;
        this.voucherSeriesService = voucherSeriesService;
        this.voucherCounterService = voucherCounterService;
    }

    @Override
    public ResponseEntity<VoucherResponse> createPaymentVoucher(
            PaymentVoucherRequest paymentVoucherRequest
    ) {
        VoucherResponse response = voucherIntakeService.processPayment(paymentVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoucherResponse> createReceiptVoucher(
            ReceiptVoucherRequest receiptVoucherRequest
    ) {
        VoucherResponse response = voucherIntakeService.processReceipt(receiptVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoucherResponse> createContraVoucher(
            ContraVoucherRequest contraVoucherRequest
    ) {
        VoucherResponse response = voucherIntakeService.processContra(contraVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<VoucherResponse> createJournalVoucher(
            JournalVoucherRequest journalVoucherRequest
    ) {
        VoucherResponse response = voucherIntakeService.processJournal(journalVoucherRequest);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CustomVoucherSeries> createVoucherSeries(
            CreateSeriesRequest createSeriesRequest) {
        return ResponseEntity.ok(voucherSeriesService.createSeries(createSeriesRequest));
    }

    @Override
    public ResponseEntity<CustomVoucherSeriesListResponse> listVoucherSeries(
            VoucherType voucherType, String financialYear, Integer page, Integer size, List<String> sort) {
        int safePage = page == null ? 0 : page;
        int safeSize = size == null ? 20 : size;
        return ResponseEntity.ok(
                voucherSeriesService.listSeries(voucherType, financialYear, safePage, safeSize, sort));
    }

    @Override
    public ResponseEntity<CustomVoucherSeries> updateVoucherSeries(
            Long id, UpdateSeriesRequest updateSeriesRequest) {
        return ResponseEntity.ok(voucherSeriesService.updateSeries(id, updateSeriesRequest));
    }

    @Override
    public ResponseEntity<NextBillNoResponse> getNextBillNo(
            String seriesCode, VoucherType voucherType, String financialYear) {
        return ResponseEntity.ok(
                voucherSeriesService.peekNextBillNo(seriesCode, voucherType, financialYear));
    }

    @Override
    public ResponseEntity<NextVoucherNumberResponse> getNextVoucherNumber(
            VoucherType voucherType) {
        Long next = voucherCounterService.peekNextNumber(voucherType);
        return ResponseEntity.ok(new NextVoucherNumberResponse().nextVoucherNumber(next));
    }
}
