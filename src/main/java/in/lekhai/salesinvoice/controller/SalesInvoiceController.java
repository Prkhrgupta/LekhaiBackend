package in.lekhai.salesinvoice.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.SalesInvoiceApi;
import in.lekhai.contract.model.SalesInvoicePreviewResponse;
import in.lekhai.contract.model.SalesInvoiceRequest;
import in.lekhai.contract.model.SalesInvoiceResponse;
import in.lekhai.salesinvoice.service.SalesInvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class SalesInvoiceController implements SalesInvoiceApi {

    private final SalesInvoiceService salesInvoiceService;

    public SalesInvoiceController(SalesInvoiceService salesInvoiceService) {
        this.salesInvoiceService = salesInvoiceService;
    }

    @Override
    public ResponseEntity<SalesInvoiceResponse> createSalesInvoice(SalesInvoiceRequest salesInvoiceRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesInvoiceService.create(salesInvoiceRequest));
    }

    @Override
    public ResponseEntity<SalesInvoicePreviewResponse> previewSalesInvoice(SalesInvoiceRequest salesInvoiceRequest) {
        return ResponseEntity.ok(salesInvoiceService.preview(salesInvoiceRequest));
    }

    @Override
    public ResponseEntity<SalesInvoiceResponse> getSalesInvoice(Long id) {
        return ResponseEntity.ok(salesInvoiceService.getById(id));
    }
}
