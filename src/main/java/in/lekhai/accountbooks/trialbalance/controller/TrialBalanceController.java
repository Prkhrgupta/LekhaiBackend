package in.lekhai.accountbooks.trialbalance.controller;

import in.lekhai.accountbooks.trialbalance.service.TrialBalanceService;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.TrialBalanceApi;
import in.lekhai.contract.model.TrialBalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class TrialBalanceController implements TrialBalanceApi {

    private final TrialBalanceService trialBalanceService;

    public TrialBalanceController(TrialBalanceService trialBalanceService) {
        this.trialBalanceService = trialBalanceService;
    }

    @Override
    public ResponseEntity<TrialBalanceResponse> getTrialBalance(
            LocalDate fromDate,
            LocalDate toDate,
            String format,
            Boolean withNilBalance
    ) {
        TrialBalanceResponse response = trialBalanceService.getTrialBalance(
                fromDate, toDate, format, Boolean.TRUE.equals(withNilBalance));
        return ResponseEntity.ok(response);
    }
}
