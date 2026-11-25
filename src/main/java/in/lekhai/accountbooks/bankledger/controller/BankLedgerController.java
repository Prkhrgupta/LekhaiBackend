package in.lekhai.accountbooks.bankledger.controller;

import in.lekhai.accountbooks.bankledger.service.BankLedgerService;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.BankLedgerApi;
import in.lekhai.contract.model.BankLedgerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class BankLedgerController implements BankLedgerApi {

    private final BankLedgerService bankLedgerService;

    public BankLedgerController(BankLedgerService bankLedgerService) {
        this.bankLedgerService = bankLedgerService;
    }

    @Override
    public ResponseEntity<BankLedgerResponse> getBankLedger(
            Long bankLedgerId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        BankLedgerResponse response = bankLedgerService.getBankLedger(
                bankLedgerId,
                fromDate,
                toDate
        );
        return ResponseEntity.ok(response);
    }
}
