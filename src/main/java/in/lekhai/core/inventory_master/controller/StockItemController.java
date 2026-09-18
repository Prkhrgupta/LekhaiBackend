package in.lekhai.core.inventory_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.StockItemApi;
import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.StockItemRequest;
import in.lekhai.contract.model.StockItemResponse;
import in.lekhai.contract.model.StockItemSearchableField;
import in.lekhai.contract.model.StockItemSummaryPageResponse;
import in.lekhai.core.inventory_master.service.StockItemService;
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
public class StockItemController implements StockItemApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final StockItemService stockItemService;

    public StockItemController(StockItemService stockItemService) {
        this.stockItemService = stockItemService;
    }

    @Override
    public ResponseEntity<StockItemResponse> createStockItem(@Valid StockItemRequest request) {
        log.info("Got a request to create stock item {} :: {}", ShopContext.getShopCode(), request.toString());
        StockItemResponse response = stockItemService.createStockItem(request);
        log.info("Successfully created stock item {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteStockItem(Long id) {
        log.info("Got a request to delete stock item {} :: id {}", ShopContext.getShopCode(), id);
        stockItemService.deleteStockItem(id);
        log.info("Successfully deleted stock item {} :: id {}", ShopContext.getShopCode(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<StockItemResponse> getStockItem(Long id) {
        log.info("Got a request to fetch stock item {} :: id {}", ShopContext.getShopCode(), id);
        StockItemResponse response = stockItemService.getStockItemById(id);
        log.info("Successfully fetched stock item {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getStockItemDropdownOptions() {
        log.info("Got a request to list all stock items {}", ShopContext.getShopCode());
        List<DropdownItem> response = stockItemService.listStockItems();
        log.info("Successfully listed all stock items {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<StockItemResponse> updateStockItem(Long id, StockItemRequest request) {
        log.info("Got a request to update stock item {} :: {}", ShopContext.getShopCode(), request.toString());
        StockItemResponse response = stockItemService.updateStockItem(id, request);
        log.info("Successfully updated stock item {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<StockItemSummaryPageResponse> getStockItemSummaries(
            @Valid StockItemSearchableField searchableField,
            @Valid String searchText,
            Pageable pageable) {
        log.info("Got a request to fetch stock item summary {}", ShopContext.getShopCode());
        StockItemSummaryPageResponse response = stockItemService.listStockItemSummaries(
                searchableField, searchText, pageable);
        log.info("Successfully fetched stock item summary {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
