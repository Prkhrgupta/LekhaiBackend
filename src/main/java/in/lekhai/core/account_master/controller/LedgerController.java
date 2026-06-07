package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.LedgerApi;
import in.lekhai.contract.model.LedgerRequest;
import in.lekhai.contract.model.LedgerResponse;
import in.lekhai.contract.model.LedgerSummaryResponse;
import in.lekhai.core.account_master.service.LedgerService;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public ResponseEntity<LedgerSummaryResponse> getLedgerSummaries(
            Integer page,
            Integer pageSize,
            String search,
            String searchField,
            String sortBy,
            String sortOrder
    ) {
        log.info("Got a request to fetch ledger summary {} :: page={}, pageSize={}, search={}, searchField={}, sortBy={}, sortOrder={}",
                ShopContext.getShopCode(), page, pageSize, search, searchField, sortBy, sortOrder);
        LedgerSummaryResponse response = ledgerService.listLedgerSummaries(
                page, pageSize, search, searchField, sortBy, sortOrder
        );
        log.info("Successfully fetched ledger summary {} :: page={}, totalItems={}",
                ShopContext.getShopCode(),
                response.getPagination().getPage(),
                response.getPagination().getTotalItems());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<LedgerResponse>> listLedgers() {
        log.info("Got a request to list all ledgers {}", ShopContext.getShopCode());
        List<LedgerResponse> response = ledgerService.listLedgers();
        log.info("Successfully listed all ledgers {}", ShopContext.getShopCode());
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
