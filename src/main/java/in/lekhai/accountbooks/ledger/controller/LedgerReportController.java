package in.lekhai.accountbooks.ledger.controller;

import in.lekhai.accountbooks.ledger.service.LedgerReportService;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.AccountLedgerApi;
import in.lekhai.contract.model.AccountLedgerPageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class LedgerReportController implements AccountLedgerApi {
    private final LedgerReportService ledgerReportService;

    public LedgerReportController(
            LedgerReportService ledgerReportService
    ) {
        this.ledgerReportService = ledgerReportService;
    }

    @Override
    public ResponseEntity<AccountLedgerPageResponse> getAccountLedgerEntries(
            Long ledgerId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        AccountLedgerPageResponse accountLedgerEntries =
                ledgerReportService.getAccountLedgerEntries(ledgerId, fromDate, toDate, pageable);
        return ResponseEntity.ok().body(accountLedgerEntries);
    }
}
