package in.lekhai.accountbooks.balancesheet.controller;

import in.lekhai.accountbooks.balancesheet.service.BalanceSheetService;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.BalanceSheetApi;
import in.lekhai.contract.model.BalanceSheetResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class BalanceSheetController implements BalanceSheetApi {

    private final BalanceSheetService balanceSheetService;

    public BalanceSheetController(BalanceSheetService balanceSheetService) {
        this.balanceSheetService = balanceSheetService;
    }

    @Override
    public ResponseEntity<BalanceSheetResponse> getBalanceSheet(
            LocalDate fromDate,
            LocalDate toDate,
            Boolean booksClosed,
            String format
    ) {
        BalanceSheetResponse response = balanceSheetService.getBalanceSheet(
                fromDate, toDate, Boolean.TRUE.equals(booksClosed), format);
        return ResponseEntity.ok(response);
    }
}
