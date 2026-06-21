package in.lekhai.core.inventory_master.service;

import in.lekhai.contract.model.*;
import in.lekhai.core.inventory_master.domain.Commodity;
import in.lekhai.core.inventory_master.domain.ItemCategory;
import in.lekhai.core.inventory_master.domain.ItemFactory;
import in.lekhai.core.inventory_master.domain.StockItem;
import in.lekhai.core.inventory_master.repository.CommodityRepository;
import in.lekhai.core.inventory_master.repository.ItemCategoryRepository;
import in.lekhai.core.inventory_master.repository.ItemFactoryRepository;
import in.lekhai.core.inventory_master.repository.StockItemRepository;
import in.lekhai.error.controller.stockitem.exception.StockItemNotFoundException;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class StockItemService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final StockItemRepository stockItemRepository;
    private final CommodityRepository commodityRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemFactoryRepository itemFactoryRepository;

    public StockItemService(StockItemRepository stockItemRepository,
                            CommodityRepository commodityRepository,
                            ItemCategoryRepository itemCategoryRepository,
                            ItemFactoryRepository itemFactoryRepository) {
        this.stockItemRepository = stockItemRepository;
        this.commodityRepository = commodityRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.itemFactoryRepository = itemFactoryRepository;
    }

    @ShopContextTransactional
    public StockItemResponse createStockItem(StockItemRequest request) {
        StockItem stockItem = mapToEntity(request, new StockItem());
        StockItem saved = stockItemRepository.save(stockItem);
        log.info("Saved stock item :: id {}", saved.getId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public StockItemResponse updateStockItem(Long id, StockItemRequest request) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new StockItemNotFoundException(id));
        mapToEntity(request, stockItem);
        StockItem saved = stockItemRepository.save(stockItem);
        log.info("Updated stock item :: id {}", saved.getId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public void deleteStockItem(Long id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new StockItemNotFoundException(id));
        stockItem.setDeleted(Boolean.TRUE);
        stockItemRepository.save(stockItem);
        log.info("Soft-deleted stock item :: id {}", stockItem.getId());
    }

    @ShopContextTransactional
    public StockItemResponse getStockItemById(Long id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new StockItemNotFoundException(id));
        return mapToResponse(stockItem);
    }

    @ShopContextTransactional
    public List<DropdownItem> listStockItems() {
        return StreamSupport.stream(stockItemRepository.findAll().spliterator(), false)
                .filter(stockItem -> !Boolean.TRUE.equals(stockItem.getDeleted()))
                .map(stockItem -> new DropdownItem().id(stockItem.getId()).label(stockItem.getItemName()))
                .toList();
    }

    @ShopContextTransactional
    public StockItemSummaryPageResponse listStockItemSummaries(
            StockItemSearchableField searchableField,
            String searchText,
            Pageable pageable) {
        Page<StockItem> stockItemsPage;
        if (searchText != null && !searchText.trim().isEmpty() && searchableField == StockItemSearchableField.NAME) {
            List<StockItem> stockItems = stockItemRepository.findActiveByNameContainingIgnoreCase(
                    searchText.trim(), pageable.getPageSize(), pageable.getOffset());
            long total = stockItemRepository.countActiveByNameContainingIgnoreCase(searchText.trim());
            stockItemsPage = new PageImpl<>(stockItems, pageable, total);
        } else {
            List<StockItem> stockItems = stockItemRepository.findAllActive(pageable.getPageSize(), pageable.getOffset());
            long total = stockItemRepository.countAllActive();
            stockItemsPage = new PageImpl<>(stockItems, pageable, total);
        }

        List<StockItem> pageContent = stockItemsPage.getContent();
        Map<Long, Commodity> commodityById = fetchByIds(
                commodityRepository, pageContent, StockItem::getCommodityId, Commodity::getItemId);
        Map<Long, ItemCategory> categoryById = fetchByIds(
                itemCategoryRepository, pageContent, StockItem::getItemCategoryId, ItemCategory::getId);
        Map<Long, ItemFactory> factoryById = fetchByIds(
                itemFactoryRepository, pageContent, StockItem::getItemFactoryId, ItemFactory::getId);

        List<StockItemResponse> data = pageContent.stream()
                .map(stockItem -> buildResponse(
                        stockItem,
                        getOrNull(commodityById, stockItem.getCommodityId()),
                        getOrNull(categoryById, stockItem.getItemCategoryId()),
                        getOrNull(factoryById, stockItem.getItemFactoryId())))
                .toList();

        return new StockItemSummaryPageResponse()
                .data(data)
                .pagination(new PaginationMeta()
                        .page(stockItemsPage.getNumber())
                        .size(stockItemsPage.getSize())
                        .totalElements(stockItemsPage.getTotalElements())
                        .totalPages(stockItemsPage.getTotalPages()));
    }

    private StockItem mapToEntity(StockItemRequest request, StockItem stockItem) {
        stockItem.setFinishedRawMaterial(request.getFinishedRawMaterial() == null ? null : request.getFinishedRawMaterial().getValue());
        stockItem.setItemCategoryId(request.getItemCategoryId());
        stockItem.setItemFactoryId(request.getItemFactoryId());
        stockItem.setItemName(request.getItemName());
        stockItem.setPurchasePrice(toBigDecimal(request.getPurchasePrice()));
        stockItem.setSalePrice(toBigDecimal(request.getSalePrice()));
        stockItem.setCommodityId(request.getCommodityId());
        stockItem.setRatePer(request.getRatePer() == null ? null : request.getRatePer().getValue());
        stockItem.setOpeningPcs(toBigDecimal(request.getOpeningPcs()));
        stockItem.setOpeningMeter(toBigDecimal(request.getOpeningMeter()));
        stockItem.setOpeningRate(toBigDecimal(request.getOpeningRate()));
        stockItem.setOpeningValue(toBigDecimal(request.getOpeningValue()));
        return stockItem;
    }

    private StockItemResponse mapToResponse(StockItem stockItem) {
        Commodity commodity = stockItem.getCommodityId() != null
                ? commodityRepository.findById(stockItem.getCommodityId()).orElse(null)
                : null;
        ItemCategory itemCategory = stockItem.getItemCategoryId() != null
                ? itemCategoryRepository.findById(stockItem.getItemCategoryId()).orElse(null)
                : null;
        ItemFactory itemFactory = stockItem.getItemFactoryId() != null
                ? itemFactoryRepository.findById(stockItem.getItemFactoryId()).orElse(null)
                : null;

        return buildResponse(stockItem, commodity, itemCategory, itemFactory);
    }

    /**
     * Batch-loads the entities referenced by {@code stockItems} via a single {@code findAllById} call,
     * keyed by their id. Avoids the per-row lookups (N+1) that {@link #mapToResponse} performs.
     */
    private static <T> Map<Long, T> fetchByIds(CrudRepository<T, Long> repository,
                                               List<StockItem> stockItems,
                                               Function<StockItem, Long> idExtractor,
                                               Function<T, Long> keyExtractor) {
        Set<Long> ids = stockItems.stream()
                .map(idExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, T> byId = new HashMap<>();
        repository.findAllById(ids).forEach(entity -> byId.put(keyExtractor.apply(entity), entity));
        return byId;
    }

    private static <T> T getOrNull(Map<Long, T> byId, Long id) {
        return id == null ? null : byId.get(id);
    }

    private StockItemResponse buildResponse(StockItem stockItem,
                                            Commodity commodity,
                                            ItemCategory itemCategory,
                                            ItemFactory itemFactory) {
        return new StockItemResponse()
                .id(stockItem.getId())
                .finishedRawMaterial(stockItem.getFinishedRawMaterial() == null
                        ? null : FinishedRawMaterial.fromValue(stockItem.getFinishedRawMaterial()))
                .itemCategoryId(stockItem.getItemCategoryId())
                .itemCategoryName(itemCategory == null ? null : itemCategory.getName())
                .itemFactoryId(stockItem.getItemFactoryId())
                .itemFactoryName(itemFactory == null ? null : itemFactory.getName())
                .itemName(stockItem.getItemName())
                .purchasePrice(toDouble(stockItem.getPurchasePrice()))
                .salePrice(toDouble(stockItem.getSalePrice()))
                .commodityId(stockItem.getCommodityId())
                .commodityName(commodity == null ? null : commodity.getItemName())
                .hsnCode(commodity == null ? null : commodity.getHsnSacCode())
                .gstPercentage(commodity == null ? null : toDouble(commodity.getGstRateSale()))
                .ratePer(stockItem.getRatePer() == null
                        ? null : RatePerUnit.fromValue(stockItem.getRatePer()))
                .openingPcs(toDouble(stockItem.getOpeningPcs()))
                .openingMeter(toDouble(stockItem.getOpeningMeter()))
                .openingRate(toDouble(stockItem.getOpeningRate()))
                .openingValue(toDouble(stockItem.getOpeningValue()));
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
