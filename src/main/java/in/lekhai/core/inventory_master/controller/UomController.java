package in.lekhai.core.inventory_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.UomApi;
import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.UomRequest;
import in.lekhai.contract.model.UomResponse;
import in.lekhai.contract.model.UomSearchableField;
import in.lekhai.contract.model.UomSummaryPageResponse;
import in.lekhai.core.inventory_master.service.UomService;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class UomController implements UomApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final UomService uomService;

    public UomController(UomService uomService) {
        this.uomService = uomService;
    }

    @Override
    public ResponseEntity<UomResponse> createUom(UomRequest request) {
        log.info("Got a request to create UOM {} :: {}", ShopContext.getShopCode(), request.toString());
        UomResponse response = uomService.createUom(request);
        log.info("Successfully created UOM {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteUom(Long id) {
        log.info("Got a request to delete UOM {} :: id {}", ShopContext.getShopCode(), id);
        uomService.deleteUom(id);
        log.info("Successfully deleted UOM {} :: id {}", ShopContext.getShopCode(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<UomResponse> getUom(Long id) {
        log.info("Got a request to fetch UOM {} :: id {}", ShopContext.getShopCode(), id);
        UomResponse response = uomService.getUomById(id);
        log.info("Successfully fetched UOM {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getUomDropdownOptions() {
        log.info("Got a request to list all UOMs {}", ShopContext.getShopCode());
        List<DropdownItem> response = uomService.listUoms();
        log.info("Successfully listed all UOMs {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UomResponse> updateUom(Long id, UomRequest request) {
        log.info("Got a request to update UOM {} :: {}", ShopContext.getShopCode(), request.toString());
        UomResponse response = uomService.updateUom(id, request);
        log.info("Successfully updated UOM {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UomSummaryPageResponse> getUomSummaries(
            UomSearchableField searchableField,
            String searchText,
            Pageable pageable) {
        log.info("Got a request to fetch UOM summary {}", ShopContext.getShopCode());
        UomSummaryPageResponse response = uomService.listUomSummaries(searchableField, searchText, pageable);
        log.info("Successfully fetched UOM summary {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
