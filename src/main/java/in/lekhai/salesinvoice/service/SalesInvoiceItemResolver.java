package in.lekhai.salesinvoice.service;

import in.lekhai.contract.model.SalesInvoiceItemRequest;
import in.lekhai.core.inventory_master.domain.Commodity;
import in.lekhai.core.inventory_master.domain.StockItem;
import in.lekhai.core.inventory_master.domain.Uom;
import in.lekhai.core.inventory_master.repository.CommodityRepository;
import in.lekhai.core.inventory_master.repository.ItemCategoryRepository;
import in.lekhai.core.inventory_master.repository.StockItemRepository;
import in.lekhai.core.inventory_master.repository.UomRepository;
import in.lekhai.salesinvoice.calc.InvoiceModels.LineInput;
import in.lekhai.salesinvoice.service.PreparedInvoice.LineSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Freezes stock item, HSN, category, UOM and tax-rate data for each requested line.
 */
@Component
class SalesInvoiceItemResolver {

    private final StockItemRepository stockItemRepository;
    private final CommodityRepository commodityRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final UomRepository uomRepository;

    SalesInvoiceItemResolver(StockItemRepository stockItemRepository, CommodityRepository commodityRepository,
                             ItemCategoryRepository itemCategoryRepository, UomRepository uomRepository) {
        this.stockItemRepository = stockItemRepository;
        this.commodityRepository = commodityRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.uomRepository = uomRepository;
    }

    LineSnapshot snapshot(SalesInvoiceItemRequest request) {
        StockItem item = stockItemRepository.findById(request.getStockItemId())
                .filter(found -> !Boolean.TRUE.equals(found.getDeleted()))
                .orElseThrow(() -> SalesInvoiceErrors.notFound(
                        String.format("Stock item [%d] not found", request.getStockItemId())));
        Commodity commodity = requireCommodity(item);
        Uom uom = uomRepository.findById(item.getPrimaryUomId()).orElseThrow(
                () -> SalesInvoiceErrors.unprocessable("UOM missing for item " + item.getItemName()));
        return new LineSnapshot(item.getId(), item.getItemName(), commodity.getHsnSacCode(),
                categoryName(item), uom.getId(), uom.getQuantityCode(),
                orZero(commodity.getGstRate()), orZero(commodity.getCessPercentage()), costRate(item));
    }

    LineInput toInput(SalesInvoiceItemRequest request, LineSnapshot snapshot) {
        requireValid(request, snapshot);
        BigDecimal discount = request.getDiscountPct() == null ? BigDecimal.ZERO
                : BigDecimal.valueOf(request.getDiscountPct());
        return new LineInput(BigDecimal.valueOf(request.getQuantity()), BigDecimal.valueOf(request.getRate()),
                discount, snapshot.gstRate(), snapshot.cessRate());
    }

    List<LineSnapshot> snapshots(List<SalesInvoiceItemRequest> requests) {
        return requests.stream().map(this::snapshot).toList();
    }

    private Commodity requireCommodity(StockItem item) {
        if (item.getCommodityId() == null) {
            throw SalesInvoiceErrors.unprocessable(
                    String.format("Item '%s' has no commodity (HSN/GST) configured", item.getItemName()));
        }
        return commodityRepository.findById(item.getCommodityId()).orElseThrow(
                () -> SalesInvoiceErrors.unprocessable("Commodity missing for item " + item.getItemName()));
    }

    private String categoryName(StockItem item) {
        if (item.getItemCategoryId() == null) {
            return null;
        }
        return itemCategoryRepository.findById(item.getItemCategoryId()).map(cat -> cat.getName()).orElse(null);
    }

    // Why: stock valuation needs a cost; purchase price first, opening rate as the documented fallback.
    private BigDecimal costRate(StockItem item) {
        if (item.getPurchasePrice() != null && item.getPurchasePrice().signum() > 0) {
            return item.getPurchasePrice();
        }
        return orZero(item.getOpeningRate());
    }

    private void requireValid(SalesInvoiceItemRequest request, LineSnapshot snapshot) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw SalesInvoiceErrors.badRequest("Quantity must be greater than zero for " + snapshot.itemName());
        }
        if (request.getRate() == null || request.getRate() < 0) {
            throw SalesInvoiceErrors.badRequest("Rate must not be negative for " + snapshot.itemName());
        }
        Double pct = request.getDiscountPct();
        if (pct != null && (pct < 0 || pct > 100)) {
            throw SalesInvoiceErrors.badRequest("Discount % must be between 0 and 100 for " + snapshot.itemName());
        }
    }

    private BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
