package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.SaleLedgerSettingApi;
import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.SaleLedgerSettingRequest;
import in.lekhai.contract.model.SaleLedgerSettingResponse;
import in.lekhai.contract.model.SaleLedgerSettingSearchableField;
import in.lekhai.contract.model.SaleLedgerSettingSummaryPageResponse;
import in.lekhai.core.account_master.service.SaleLedgerSettingService;
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
public class SaleLedgerSettingController implements SaleLedgerSettingApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SaleLedgerSettingService saleLedgerSettingService;

    public SaleLedgerSettingController(SaleLedgerSettingService saleLedgerSettingService) {
        this.saleLedgerSettingService = saleLedgerSettingService;
    }

    @Override
    public ResponseEntity<SaleLedgerSettingResponse> createSaleLedgerSetting(
            @Valid SaleLedgerSettingRequest request) {
        log.info("Got a request to create sale ledger setting {} :: {}", ShopContext.getShopCode(), request.toString());
        SaleLedgerSettingResponse response = saleLedgerSettingService.createSaleLedgerSetting(request);
        log.info("Successfully created sale ledger setting {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteSaleLedgerSetting(Long id) {
        log.info("Got a request to delete sale ledger setting {} :: id {}", ShopContext.getShopCode(), id);
        saleLedgerSettingService.deleteSaleLedgerSetting(id);
        log.info("Successfully deleted sale ledger setting {} :: id {}", ShopContext.getShopCode(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<SaleLedgerSettingResponse> getSaleLedgerSetting(Long id) {
        log.info("Got a request to fetch sale ledger setting {} :: id {}", ShopContext.getShopCode(), id);
        SaleLedgerSettingResponse response = saleLedgerSettingService.getSaleLedgerSettingById(id);
        log.info("Successfully fetched sale ledger setting {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getSaleLedgerSettingDropdownOptions() {
        log.info("Got a request to list all sale ledger settings {}", ShopContext.getShopCode());
        List<DropdownItem> response = saleLedgerSettingService.listSaleLedgerSettings();
        log.info("Successfully listed all sale ledger settings {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SaleLedgerSettingResponse> updateSaleLedgerSetting(
            Long id, SaleLedgerSettingRequest request) {
        log.info("Got a request to update sale ledger setting {} :: {}", ShopContext.getShopCode(), request.toString());
        SaleLedgerSettingResponse response = saleLedgerSettingService.updateSaleLedgerSetting(id, request);
        log.info("Successfully updated sale ledger setting {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SaleLedgerSettingSummaryPageResponse> getSaleLedgerSettingSummaries(
            @Valid SaleLedgerSettingSearchableField searchableField,
            @Valid String searchText,
            Pageable pageable) {
        log.info("Got a request to fetch sale ledger setting summary {}", ShopContext.getShopCode());
        SaleLedgerSettingSummaryPageResponse response = saleLedgerSettingService
                .listSaleLedgerSettingSummaries(searchableField, searchText, pageable);
        log.info("Successfully fetched sale ledger setting summary {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
