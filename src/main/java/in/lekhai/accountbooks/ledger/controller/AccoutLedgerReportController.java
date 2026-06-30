package in.lekhai.accountbooks.ledger.controller;

import in.lekhai.accountbooks.ledger.service.LedgerReportService;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.AccountLedgerApi;
import in.lekhai.contract.model.AccountLedgerPageResponse;
import in.lekhai.contract.model.AccountLedgerSearchableField;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class AccoutLedgerReportController implements AccountLedgerApi {
    private final LedgerReportService ledgerReportService;

    public AccoutLedgerReportController(
            LedgerReportService ledgerReportService
    ) {
        this.ledgerReportService = ledgerReportService;
    }

    @Override
    public ResponseEntity<AccountLedgerPageResponse> getAccountLedgerEntries(
            @NotNull Long ledgerId,
            @Valid AccountLedgerSearchableField searchableField,
            @Valid String query,
            Pageable pageable
    ) {
        AccountLedgerPageResponse accountLedgerEntries =
                ledgerReportService.getAccountLedgerEntries(ledgerId, searchableField, query, pageable);
        return ResponseEntity.ok().body(accountLedgerEntries);
    }
}
