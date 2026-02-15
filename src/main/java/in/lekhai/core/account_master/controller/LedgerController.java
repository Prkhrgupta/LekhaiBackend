package in.lekhai.core.account_master.controller;

import in.lekhai.accountmaster.ledger.api.LedgerApi;
import in.lekhai.accountmaster.ledger.dto.LedgerRequest;
import in.lekhai.accountmaster.ledger.dto.LedgerResponse;
import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.core.account_master.service.LedgerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class LedgerController implements LedgerApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final LedgerService ledgerService;

    public LedgerController(
            LedgerService ledgerService
    ) {
        this.ledgerService = ledgerService;
    }

    @Override
    public ResponseEntity<LedgerResponse> createLedger(@Valid LedgerRequest request) {
        LedgerResponse response = ledgerService.createLedger(request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LedgerResponse> getLedger(Long id) {
        LedgerResponse response = ledgerService.getLedgerById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<LedgerResponse>> listLedgers() {
        List<LedgerResponse> response = ledgerService.listLedgers();
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LedgerResponse> updateLedger(Long id, @Valid LedgerRequest request) {
        LedgerResponse response = ledgerService.updateLedger(id, request);
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/create")
//    public ResponseEntity<Result<LedgerResponse>> createLedger(@RequestBody @Valid LedgerRequest request) {
//        log.info("Got a request to create ledger for shop {} :: {}", request.name(), request);
//        LedgerResponse response = ledgerService.createLedger(request);
//        log.info("Successfully created ledger for shop {}", request.name());
//        return ResponseEntity.ok(Result.success(String.format("Ledger Created for %s", request.name()), response));
//    }
//
//    @GetMapping("/list-all")
//    public ResponseEntity<Result<List<LedgerResponse>>> listLedgers() {
//        List<LedgerResponse> response = ledgerService.listLedgers();
//        return ResponseEntity.ok(Result.success(response));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Result<LedgerResponse>> getLedger(@PathVariable Long id) {
//        log.info("Got a request to fetch ledger with id {}", id);
//        LedgerResponse response = ledgerService.getLedgerById(id);
//        return ResponseEntity.ok(Result.success(response));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Result<LedgerResponse>> updateLedger(@PathVariable Long id,
//            @RequestBody @Valid LedgerRequest request) {
//        log.info("Got a request to update ledger with id {} :: {}", id, request);
//        LedgerResponse response = ledgerService.updateLedger(id, request);
//        log.info("Successfully updated ledger with id {}", id);
//        return ResponseEntity.ok(Result.success(String.format("Ledger Updated for %s", request.name()), response));
//    }
}
