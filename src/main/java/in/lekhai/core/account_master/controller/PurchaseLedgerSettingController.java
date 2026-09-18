package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.PurchaseLedgerSettingApi;
import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.PurchaseLedgerSettingRequest;
import in.lekhai.contract.model.PurchaseLedgerSettingResponse;
import in.lekhai.contract.model.PurchaseLedgerSettingSearchableField;
import in.lekhai.contract.model.PurchaseLedgerSettingSummaryPageResponse;
import in.lekhai.core.account_master.service.PurchaseLedgerSettingService;
import in.lekhai.shop.context.model.ShopContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class PurchaseLedgerSettingController implements PurchaseLedgerSettingApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final PurchaseLedgerSettingService purchaseLedgerSettingService;

    public PurchaseLedgerSettingController(PurchaseLedgerSettingService purchaseLedgerSettingService) {
        this.purchaseLedgerSettingService = purchaseLedgerSettingService;
    }

    @Override
    public ResponseEntity<PurchaseLedgerSettingResponse> createPurchaseLedgerSetting(
            @Valid PurchaseLedgerSettingRequest request) {
        log.info("Got a request to create purchase ledger setting {} :: {}",
                ShopContext.getShopCode(), request.toString());
        PurchaseLedgerSettingResponse response = purchaseLedgerSettingService.createPurchaseLedgerSetting(request);
        log.info("Successfully created purchase ledger setting {} :: id {}",
                ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deletePurchaseLedgerSetting(Long id) {
        log.info("Got a request to delete purchase ledger setting {} :: id {}", ShopContext.getShopCode(), id);
        purchaseLedgerSettingService.deletePurchaseLedgerSetting(id);
        log.info("Successfully deleted purchase ledger setting {} :: id {}", ShopContext.getShopCode(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PurchaseLedgerSettingResponse> getPurchaseLedgerSetting(Long id) {
        log.info("Got a request to fetch purchase ledger setting {} :: id {}", ShopContext.getShopCode(), id);
        PurchaseLedgerSettingResponse response = purchaseLedgerSettingService.getPurchaseLedgerSettingById(id);
        log.info("Successfully fetched purchase ledger setting {} :: id {}",
                ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getPurchaseLedgerSettingDropdownOptions() {
        log.info("Got a request to list all purchase ledger settings {}", ShopContext.getShopCode());
        List<DropdownItem> response = purchaseLedgerSettingService.listPurchaseLedgerSettings();
        log.info("Successfully listed all purchase ledger settings {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PurchaseLedgerSettingResponse> updatePurchaseLedgerSetting(
            Long id, PurchaseLedgerSettingRequest request) {
        log.info("Got a request to update purchase ledger setting {} :: {}",
                ShopContext.getShopCode(), request.toString());
        PurchaseLedgerSettingResponse response = purchaseLedgerSettingService
                .updatePurchaseLedgerSetting(id, request);
        log.info("Successfully updated purchase ledger setting {} :: id {}",
                ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<PurchaseLedgerSettingSummaryPageResponse> getPurchaseLedgerSettingSummaries(
            @Valid PurchaseLedgerSettingSearchableField searchableField,
            @Valid String searchText,
            Pageable pageable) {
        log.info("Got a request to fetch purchase ledger setting summary {}", ShopContext.getShopCode());
        PurchaseLedgerSettingSummaryPageResponse response = purchaseLedgerSettingService
                .listPurchaseLedgerSettingSummaries(searchableField, searchText, pageable);
        log.info("Successfully fetched purchase ledger setting summary {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
