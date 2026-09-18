package in.lekhai.core.inventory_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.inventory_master.domain.ItemFactory;
import in.lekhai.core.inventory_master.repository.ItemFactoryRepository;
import in.lekhai.error.controller.itemfactory.exception.ItemFactoryNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class ItemFactoryService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ItemFactoryRepository itemFactoryRepository;

    public ItemFactoryService(ItemFactoryRepository itemFactoryRepository) {
        this.itemFactoryRepository = itemFactoryRepository;
    }

    @ShopContextTransactional
    public ItemFactoryResponse createItemFactory(ItemFactoryRequest request) {
        ItemFactory itemFactory = mapToEntity(request, new ItemFactory());
        ItemFactory saved = itemFactoryRepository.save(itemFactory);
        log.info("Saved item factory :: id {}", saved.getId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public ItemFactoryResponse updateItemFactory(Long id, ItemFactoryRequest request) {
        ItemFactory itemFactory = itemFactoryRepository.findById(id)
                .orElseThrow(() -> new ItemFactoryNotFoundException(id));
        mapToEntity(request, itemFactory);
        ItemFactory saved = itemFactoryRepository.save(itemFactory);
        log.info("Updated item factory :: id {}", saved.getId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public void deleteItemFactory(Long id) {
        ItemFactory itemFactory = itemFactoryRepository.findById(id)
                .orElseThrow(() -> new ItemFactoryNotFoundException(id));
        itemFactory.setDeleted(Boolean.TRUE);
        itemFactoryRepository.save(itemFactory);
        log.info("Soft-deleted item factory :: id {}", itemFactory.getId());
    }

    @ShopContextTransactional
    public ItemFactoryResponse getItemFactoryById(Long id) {
        ItemFactory itemFactory = itemFactoryRepository.findById(id)
                .orElseThrow(() -> new ItemFactoryNotFoundException(id));
        return mapToResponse(itemFactory);
    }

    @ShopContextTransactional
    public List<DropdownItem> listItemFactories() {
        return StreamSupport.stream(itemFactoryRepository.findAll().spliterator(), false)
                .filter(itemFactory -> !Boolean.TRUE.equals(itemFactory.getDeleted()))
                .map(itemFactory -> new DropdownItem().id(itemFactory.getId()).label(itemFactory.getName()))
                .toList();
    }

    @ShopContextTransactional
    public ItemFactorySummaryPageResponse listItemFactorySummaries(
            ItemFactorySearchableField searchableField,
            String searchText,
            Pageable pageable) {
        Page<ItemFactory> itemFactoriesPage;
        if (searchText != null && !searchText.trim().isEmpty() && searchableField == ItemFactorySearchableField.NAME) {
            List<ItemFactory> itemFactories = itemFactoryRepository.findActiveByNameContainingIgnoreCase(
                    searchText.trim(), pageable.getPageSize(), pageable.getOffset());
            long total = itemFactoryRepository.countActiveByNameContainingIgnoreCase(searchText.trim());
            itemFactoriesPage = new PageImpl<>(itemFactories, pageable, total);
        } else {
            List<ItemFactory> itemFactories = itemFactoryRepository.findAllActive(pageable.getPageSize(), pageable.getOffset());
            long total = itemFactoryRepository.countAllActive();
            itemFactoriesPage = new PageImpl<>(itemFactories, pageable, total);
        }

        List<ItemFactoryResponse> data = itemFactoriesPage.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new ItemFactorySummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(itemFactoriesPage.getNumber())
                        .size(itemFactoriesPage.getSize())
                        .totalElements(itemFactoriesPage.getTotalElements())
                        .totalPages(itemFactoriesPage.getTotalPages()));
    }

    private ItemFactory mapToEntity(ItemFactoryRequest request, ItemFactory itemFactory) {
        itemFactory.setName(request.getName());
        itemFactory.setPercentage(toBigDecimal(request.getPercentage()));
        return itemFactory;
    }

    private ItemFactoryResponse mapToResponse(ItemFactory itemFactory) {
        return new ItemFactoryResponse()
                .id(itemFactory.getId())
                .name(itemFactory.getName())
                .percentage(toDouble(itemFactory.getPercentage()));
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
