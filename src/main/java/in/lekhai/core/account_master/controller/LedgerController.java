package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.account_master.dto.ledger.LedgerRequest;
import in.lekhai.core.account_master.dto.ledger.LedgerResponse;
import in.lekhai.core.account_master.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(
            LedgerService ledgerService
    ) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<Void>> createLedger(@RequestBody @Valid LedgerRequest request) {
        ledgerService.createLedger(request);
        return ResponseEntity.ok(Result.success("Ledger Created"));
    }

    @GetMapping("/list-all")
    public ResponseEntity<Result<List<LedgerResponse>>> listLedgers() {
        List<LedgerResponse> response = ledgerService.listLedgers();
        return ResponseEntity.ok(Result.success(response));
    }
}
