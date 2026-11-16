package in.lekhai.accountbooks.bankreconciliation.controller;

import in.lekhai.accountbooks.bankreconciliation.service.BankReconciliationService;
import in.lekhai.contract.api.BankReconciliationApi;
import in.lekhai.contract.model.BankReconciliationMode;
import in.lekhai.contract.model.BankReconciliationResponse;
import in.lekhai.contract.model.UpdateClearDateRequest;
import in.lekhai.contract.model.UpdateClearDateResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class BankReconciliationController implements BankReconciliationApi {

    private final BankReconciliationService bankReconciliationService;

    public BankReconciliationController(BankReconciliationService bankReconciliationService) {
        this.bankReconciliationService = bankReconciliationService;
    }

    @Override
    public ResponseEntity<BankReconciliationResponse> getBankReconciliation(
            Long bankLedgerId,
            LocalDate fromDate,
            LocalDate toDate,
            BankReconciliationMode mode
    ) {
        BankReconciliationResponse response = bankReconciliationService.getBankReconciliation(
                bankLedgerId, fromDate, toDate, mode
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UpdateClearDateResponse> updateClearDate(UpdateClearDateRequest request) {
        LocalDate clearDate = request.getClearDate() != null && request.getClearDate().isPresent()
                ? request.getClearDate().get()
                : null;
        UpdateClearDateResponse response = bankReconciliationService.updateClearDate(
                request.getVoucherEntryId(), clearDate
        );
        return ResponseEntity.ok(response);
    }
}
