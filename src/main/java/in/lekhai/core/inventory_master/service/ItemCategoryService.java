package in.lekhai.core.inventory_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.inventory_master.domain.ItemCategory;
import in.lekhai.core.inventory_master.repository.ItemCategoryRepository;
import in.lekhai.error.controller.itemcategory.exception.ItemCategoryNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class ItemCategoryService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ItemCategoryRepository itemCategoryRepository;

    public ItemCategoryService(ItemCategoryRepository itemCategoryRepository) {
        this.itemCategoryRepository = itemCategoryRepository;
    }

    @ShopContextTransactional
    public ItemCategoryResponse createItemCategory(ItemCategoryRequest request) {
        ItemCategory itemCategory = mapToEntity(request, new ItemCategory());
        ItemCategory saved = itemCategoryRepository.save(itemCategory);
        log.info("Saved item category :: id {}", saved.getId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public ItemCategoryResponse updateItemCategory(Long id, ItemCategoryRequest request) {
        ItemCategory itemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> new ItemCategoryNotFoundException(id));
        mapToEntity(request, itemCategory);
        ItemCategory saved = itemCategoryRepository.save(itemCategory);
        log.info("Updated item category :: id {}", saved.getId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public void deleteItemCategory(Long id) {
        ItemCategory itemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> new ItemCategoryNotFoundException(id));
        itemCategory.setDeleted(Boolean.TRUE);
        itemCategoryRepository.save(itemCategory);
        log.info("Soft-deleted item category :: id {}", itemCategory.getId());
    }

    @ShopContextTransactional
    public ItemCategoryResponse getItemCategoryById(Long id) {
        ItemCategory itemCategory = itemCategoryRepository.findById(id)
                .orElseThrow(() -> new ItemCategoryNotFoundException(id));
        return mapToResponse(itemCategory);
    }

    @ShopContextTransactional
    public List<DropdownItem> listItemCategories() {
        return StreamSupport.stream(itemCategoryRepository.findAll().spliterator(), false)
                .filter(itemCategory -> !Boolean.TRUE.equals(itemCategory.getDeleted()))
                .map(itemCategory -> new DropdownItem().id(itemCategory.getId()).label(itemCategory.getName()))
                .toList();
    }

    @ShopContextTransactional
    public ItemCategorySummaryPageResponse listItemCategorySummaries(
            ItemCategorySearchableField searchableField,
            String searchText,
            Pageable pageable) {
        Page<ItemCategory> itemCategoriesPage;
        if (searchText != null && !searchText.trim().isEmpty() && searchableField == ItemCategorySearchableField.NAME) {
            List<ItemCategory> itemCategories = itemCategoryRepository.findActiveByNameContainingIgnoreCase(
                    searchText.trim(), pageable.getPageSize(), pageable.getOffset());
            long total = itemCategoryRepository.countActiveByNameContainingIgnoreCase(searchText.trim());
            itemCategoriesPage = new PageImpl<>(itemCategories, pageable, total);
        } else {
            List<ItemCategory> itemCategories = itemCategoryRepository.findAllActive(pageable.getPageSize(), pageable.getOffset());
            long total = itemCategoryRepository.countAllActive();
            itemCategoriesPage = new PageImpl<>(itemCategories, pageable, total);
        }

        List<ItemCategoryResponse> data = itemCategoriesPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new ItemCategorySummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(itemCategoriesPage.getNumber())
                        .size(itemCategoriesPage.getSize())
                        .totalElements(itemCategoriesPage.getTotalElements())
                        .totalPages(itemCategoriesPage.getTotalPages()));
    }

    private ItemCategory mapToEntity(ItemCategoryRequest request, ItemCategory itemCategory) {
        itemCategory.setName(request.getName());
        return itemCategory;
    }

    private ItemCategoryResponse mapToResponse(ItemCategory itemCategory) {
        return new ItemCategoryResponse()
                .id(itemCategory.getId())
                .name(itemCategory.getName());
    }
}
