package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.LedgerApi;
import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.service.LedgerService;
import in.lekhai.shop.context.model.ShopContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<LedgerResponse> createLedger(LedgerRequest request) {
        log.info("Got a request to create ledger {} :: {}", ShopContext.getShopCode(), request.toString());
        LedgerResponse response = ledgerService.createLedger(request);
        log.info("Successfully created ledger {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LedgerResponse> getLedger(Long id) {
        log.info("Got a request to fetch ledger {} :: ledger id {}", ShopContext.getShopCode(), id);
        LedgerResponse response = ledgerService.getLedgerById(id);
        log.info("Successfully fetched ledger {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }
    @Override
    public ResponseEntity<LedgerBalanceResponse> getLedgerBalance(@NotNull Long ledgerId) {
        LedgerBalanceResponse res = ledgerService.calcLedgerBalance(ledgerId);
        return ResponseEntity.ok(res);
    }

    @Override
    public ResponseEntity<LedgerBalanceResponse> getLedgerBalance(@NotNull Long ledgerId) {
        LedgerBalanceResponse res = ledgerService.calcLedgerBalance(ledgerId);
        return ResponseEntity.ok(res);
    }

    @Override
    public ResponseEntity<LedgerResponse> getLedgerByGstin(@NotNull @Valid String gstIn) {
        LedgerResponse ledgerResponseByGstIn = ledgerService.getLedgerResponseByGstIn(gstIn);
        return ResponseEntity.ok(ledgerResponseByGstIn);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getLedgerDropdownOptions(@Valid List<Long> underAccountGroup) {
        log.info("Got a request to list all ledgers {}", ShopContext.getShopCode());
        List<DropdownItem> response = ledgerService.listLedgers(underAccountGroup);
        log.info("Successfully listed all ledgers {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LedgerSummaryPageResponse> getLedgerSummaries(@Valid LedgerSearchableField ledgerSearchableField,
                                                                        @Valid String query,
                                                                        Pageable pageable) {
        log.info("Got a request to fetch ledger summary {}", ShopContext.getShopCode());
        LedgerSummaryPageResponse response = ledgerService.listLedgerSummaries(ledgerSearchableField, query, pageable);
        log.info("Successfully fetched ledger summary {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<LedgerResponse> updateLedger(Long id, LedgerRequest request) {
        log.info("Got a request to update ledger {} :: {}", ShopContext.getShopCode(), request.toString());
        LedgerResponse response = ledgerService.updateLedger(id, request);
        log.info("Successfully updated ledger {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }
}
