package in.lekhai.core.inventory_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.ItemFactoryApi;
import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.ItemFactoryRequest;
import in.lekhai.contract.model.ItemFactoryResponse;
import in.lekhai.contract.model.ItemFactorySearchableField;
import in.lekhai.contract.model.ItemFactorySummaryPageResponse;
import in.lekhai.core.inventory_master.service.ItemFactoryService;
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
public class ItemFactoryController implements ItemFactoryApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ItemFactoryService itemFactoryService;

    public ItemFactoryController(ItemFactoryService itemFactoryService) {
        this.itemFactoryService = itemFactoryService;
    }

    @Override
    public ResponseEntity<ItemFactoryResponse> createItemFactory(@Valid ItemFactoryRequest request) {
        log.info("Got a request to create item factory {} :: {}", ShopContext.getShopCode(), request.toString());
        ItemFactoryResponse response = itemFactoryService.createItemFactory(request);
        log.info("Successfully created item factory {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteItemFactory(Long id) {
        log.info("Got a request to delete item factory {} :: id {}", ShopContext.getShopCode(), id);
        itemFactoryService.deleteItemFactory(id);
        log.info("Successfully deleted item factory {} :: id {}", ShopContext.getShopCode(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ItemFactoryResponse> getItemFactory(Long id) {
        log.info("Got a request to fetch item factory {} :: id {}", ShopContext.getShopCode(), id);
        ItemFactoryResponse response = itemFactoryService.getItemFactoryById(id);
        log.info("Successfully fetched item factory {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getItemFactoryDropdownOptions() {
        log.info("Got a request to list all item factories {}", ShopContext.getShopCode());
        List<DropdownItem> response = itemFactoryService.listItemFactories();
        log.info("Successfully listed all item factories {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ItemFactoryResponse> updateItemFactory(Long id, ItemFactoryRequest request) {
        log.info("Got a request to update item factory {} :: {}", ShopContext.getShopCode(), request.toString());
        ItemFactoryResponse response = itemFactoryService.updateItemFactory(id, request);
        log.info("Successfully updated item factory {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ItemFactorySummaryPageResponse> getItemFactorySummaries(
            @Valid ItemFactorySearchableField searchableField,
            @Valid String searchText,
            Pageable pageable) {
        log.info("Got a request to fetch item factory summary {}", ShopContext.getShopCode());
        ItemFactorySummaryPageResponse response = itemFactoryService.listItemFactorySummaries(
                searchableField, searchText, pageable);
        log.info("Successfully fetched item factory summary {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
