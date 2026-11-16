package in.lekhai.accountbooks.cashbook.controller;

import in.lekhai.accountbooks.cashbook.service.CashBookService;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.CashBookApi;
import in.lekhai.contract.model.CashBookResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class CashBookController implements CashBookApi {

    private final CashBookService cashBookService;

    public CashBookController(CashBookService cashBookService) {
        this.cashBookService = cashBookService;
    }

    @Override
    public ResponseEntity<CashBookResponse> getCashBook(
            LocalDate fromDate,
            LocalDate toDate,
            String format,
            Boolean singleCashSaleEntry,
            Boolean hideTransactionDate,
            String closingInterval,
            Long ledgerId
    ) {
        CashBookResponse response = cashBookService.getCashBook(
                fromDate,
                toDate,
                format,
                singleCashSaleEntry,
                hideTransactionDate,
                closingInterval,
                ledgerId
        );
        return ResponseEntity.ok(response);
    }
}
