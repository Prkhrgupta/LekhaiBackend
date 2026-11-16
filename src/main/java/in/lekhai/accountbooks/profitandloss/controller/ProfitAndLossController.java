package in.lekhai.accountbooks.profitandloss.controller;

import in.lekhai.accountbooks.profitandloss.service.ProfitAndLossService;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.ProfitAndLossApi;
import in.lekhai.contract.model.ProfitAndLossResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class ProfitAndLossController implements ProfitAndLossApi {

    private final ProfitAndLossService profitAndLossService;

    public ProfitAndLossController(ProfitAndLossService profitAndLossService) {
        this.profitAndLossService = profitAndLossService;
    }

    @Override
    public ResponseEntity<ProfitAndLossResponse> getProfitAndLoss(
            LocalDate fromDate,
            LocalDate toDate,
            String stockMethod,
            BigDecimal openingStock,
            BigDecimal closingStock,
            String format
    ) {
        ProfitAndLossResponse response = profitAndLossService.getProfitAndLoss(
                fromDate, toDate, stockMethod, openingStock, closingStock, format);
        return ResponseEntity.ok(response);
    }
}
