package in.lekhai.core.inventory_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.ItemCategoryApi;
import in.lekhai.contract.model.DropdownItem;
import in.lekhai.contract.model.ItemCategoryRequest;
import in.lekhai.contract.model.ItemCategoryResponse;
import in.lekhai.contract.model.ItemCategorySearchableField;
import in.lekhai.contract.model.ItemCategorySummaryPageResponse;
import in.lekhai.core.inventory_master.service.ItemCategoryService;
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
public class ItemCategoryController implements ItemCategoryApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final ItemCategoryService itemCategoryService;

    public ItemCategoryController(ItemCategoryService itemCategoryService) {
        this.itemCategoryService = itemCategoryService;
    }

    @Override
    public ResponseEntity<ItemCategoryResponse> createItemCategory(@Valid ItemCategoryRequest request) {
        log.info("Got a request to create item category {} :: {}", ShopContext.getShopCode(), request.toString());
        ItemCategoryResponse response = itemCategoryService.createItemCategory(request);
        log.info("Successfully created item category {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteItemCategory(Long id) {
        log.info("Got a request to delete item category {} :: id {}", ShopContext.getShopCode(), id);
        itemCategoryService.deleteItemCategory(id);
        log.info("Successfully deleted item category {} :: id {}", ShopContext.getShopCode(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ItemCategoryResponse> getItemCategory(Long id) {
        log.info("Got a request to fetch item category {} :: id {}", ShopContext.getShopCode(), id);
        ItemCategoryResponse response = itemCategoryService.getItemCategoryById(id);
        log.info("Successfully fetched item category {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getItemCategoryDropdownOptions() {
        log.info("Got a request to list all item categories {}", ShopContext.getShopCode());
        List<DropdownItem> response = itemCategoryService.listItemCategories();
        log.info("Successfully listed all item categories {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ItemCategoryResponse> updateItemCategory(Long id, ItemCategoryRequest request) {
        log.info("Got a request to update item category {} :: {}", ShopContext.getShopCode(), request.toString());
        ItemCategoryResponse response = itemCategoryService.updateItemCategory(id, request);
        log.info("Successfully updated item category {} :: id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ItemCategorySummaryPageResponse> getItemCategorySummaries(
            @Valid ItemCategorySearchableField searchableField,
            @Valid String searchText,
            Pageable pageable) {
        log.info("Got a request to fetch item category summary {}", ShopContext.getShopCode());
        ItemCategorySummaryPageResponse response = itemCategoryService.listItemCategorySummaries(
                searchableField, searchText, pageable);
        log.info("Successfully fetched item category summary {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
