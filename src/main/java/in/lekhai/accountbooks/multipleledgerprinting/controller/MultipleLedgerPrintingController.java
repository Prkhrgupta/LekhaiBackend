package in.lekhai.accountbooks.multipleledgerprinting.controller;

import in.lekhai.accountbooks.multipleledgerprinting.service.MultipleLedgerPrintingService;
import in.lekhai.contract.api.MultipleLedgerPrintingApi;
import in.lekhai.contract.model.MultipleLedgerPrintingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MultipleLedgerPrintingController implements MultipleLedgerPrintingApi {

    private final MultipleLedgerPrintingService multipleLedgerPrintingService;

    public MultipleLedgerPrintingController(MultipleLedgerPrintingService multipleLedgerPrintingService) {
        this.multipleLedgerPrintingService = multipleLedgerPrintingService;
    }

    @Override
    public ResponseEntity<MultipleLedgerPrintingResponse> getMultipleLedgerPrinting() {
        MultipleLedgerPrintingResponse response = multipleLedgerPrintingService.getMultipleLedgerPrinting();
        return ResponseEntity.ok(response);
    }
}
