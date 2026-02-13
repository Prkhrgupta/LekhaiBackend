package in.lekhai.core.inventory_master.service;

import in.lekhai.core.inventory_master.domain.Commodity;
import in.lekhai.core.inventory_master.dto.CommodityRequest;
import in.lekhai.core.inventory_master.dto.CommodityResponse;
import in.lekhai.core.inventory_master.repository.CommodityRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class CommodityService {

    private final CommodityRepository commodityRepository;

    public CommodityService(CommodityRepository commodityRepository) {
        this.commodityRepository = commodityRepository;
    }

    @ShopContextTransactional
    public CommodityResponse createCommodity(CommodityRequest request) {
        Commodity commodity = mapToEntity(request, new Commodity());
        Commodity saved = commodityRepository.save(commodity);
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public CommodityResponse updateCommodity(Long id, CommodityRequest request) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commodity not found with id: " + id));

        mapToEntity(request, commodity);
        Commodity saved = commodityRepository.save(commodity);
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public CommodityResponse getCommodity(Long id) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commodity not found with id: " + id));
        return mapToResponse(commodity);
    }

    @ShopContextTransactional
    public List<CommodityResponse> listCommodities() {
        return StreamSupport.stream(commodityRepository.findAll().spliterator(), false)
                .map(this::mapToResponse)
                .toList();
    }

    private Commodity mapToEntity(CommodityRequest request, Commodity commodity) {
        commodity.setItemName(request.itemName());
        commodity.setHsnSacCode(request.hsnSacCode());
        commodity.setDescription(request.description());
        commodity.setUom(request.uom());
        commodity.setGstRateSale(request.gstRateSale());
        commodity.setGstRatePurchase(request.gstRatePurchase());
        commodity.setIsSalePurchaseActive(request.isSalePurchaseActive());

        if (request.salesLedgerConfig() != null) {
            var sales = request.salesLedgerConfig();
            commodity.setSaleAcInStateId(sales.inStateAccountId());
            commodity.setSaleCgstPercent(sales.cgstPercent());
            commodity.setSaleSgstPercent(sales.sgstPercent());
            commodity.setSaleCessPercent(sales.cessPercent());
            commodity.setRoundOffAcId(sales.roundOffAccountId());
            commodity.setSaleAcOutStateId(sales.outStateAccountId());
            commodity.setSaleIgstPercent(sales.igstPercent());
            commodity.setSaleCessOutPercent(sales.outCessPercent());
        }

        if (request.purchaseLedgerConfig() != null) {
            var purchase = request.purchaseLedgerConfig();
            commodity.setPurchaseAcInStateId(purchase.inStateAccountId());
            commodity.setPurchaseCgstPercent(purchase.cgstPercent());
            commodity.setPurchaseSgstPercent(purchase.sgstPercent());
            commodity.setPurchaseCessPercent(purchase.cessPercent());
            commodity.setPurchaseAcOutStateId(purchase.outStateAccountId());
            commodity.setPurchaseIgstPercent(purchase.igstPercent());
            commodity.setPurchaseCessOutPercent(purchase.outCessPercent());
        }

        return commodity;
    }

    @ShopContextTransactional
    public CommodityResponse patchCommodity(Long id, CommodityRequest request) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commodity not found with id: " + id));

        patchToEntity(request, commodity);
        Commodity saved = commodityRepository.save(commodity);
        return mapToResponse(saved);
    }

    private void patchToEntity(CommodityRequest request, Commodity commodity) {
        if (request.itemName() != null)
            commodity.setItemName(request.itemName());
        if (request.hsnSacCode() != null)
            commodity.setHsnSacCode(request.hsnSacCode());
        if (request.description() != null)
            commodity.setDescription(request.description());
        if (request.uom() != null)
            commodity.setUom(request.uom());
        if (request.gstRateSale() != null)
            commodity.setGstRateSale(request.gstRateSale());
        if (request.gstRatePurchase() != null)
            commodity.setGstRatePurchase(request.gstRatePurchase());
        if (request.isSalePurchaseActive() != null)
            commodity.setIsSalePurchaseActive(request.isSalePurchaseActive());

        if (request.salesLedgerConfig() != null) {
            var sales = request.salesLedgerConfig();
            if (sales.inStateAccountId() != null)
                commodity.setSaleAcInStateId(sales.inStateAccountId());
            if (sales.cgstPercent() != null)
                commodity.setSaleCgstPercent(sales.cgstPercent());
            if (sales.sgstPercent() != null)
                commodity.setSaleSgstPercent(sales.sgstPercent());
            if (sales.cessPercent() != null)
                commodity.setSaleCessPercent(sales.cessPercent());
            if (sales.roundOffAccountId() != null)
                commodity.setRoundOffAcId(sales.roundOffAccountId());
            if (sales.outStateAccountId() != null)
                commodity.setSaleAcOutStateId(sales.outStateAccountId());
            if (sales.igstPercent() != null)
                commodity.setSaleIgstPercent(sales.igstPercent());
            if (sales.outCessPercent() != null)
                commodity.setSaleCessOutPercent(sales.outCessPercent());
        }

        if (request.purchaseLedgerConfig() != null) {
            var purchase = request.purchaseLedgerConfig();
            if (purchase.inStateAccountId() != null)
                commodity.setPurchaseAcInStateId(purchase.inStateAccountId());
            if (purchase.cgstPercent() != null)
                commodity.setPurchaseCgstPercent(purchase.cgstPercent());
            if (purchase.sgstPercent() != null)
                commodity.setPurchaseSgstPercent(purchase.sgstPercent());
            if (purchase.cessPercent() != null)
                commodity.setPurchaseCessPercent(purchase.cessPercent());
            if (purchase.outStateAccountId() != null)
                commodity.setPurchaseAcOutStateId(purchase.outStateAccountId());
            if (purchase.igstPercent() != null)
                commodity.setPurchaseIgstPercent(purchase.igstPercent());
            if (purchase.outCessPercent() != null)
                commodity.setPurchaseCessOutPercent(purchase.outCessPercent());
        }
    }

    private CommodityResponse mapToResponse(Commodity commodity) {
        var salesConfig = new CommodityResponse.SalesLedgerConfigDto(
                commodity.getSaleAcInStateId(),
                commodity.getSaleCgstPercent(),
                commodity.getSaleSgstPercent(),
                commodity.getSaleCessPercent(),
                commodity.getRoundOffAcId(),
                commodity.getSaleAcOutStateId(),
                commodity.getSaleIgstPercent(),
                commodity.getSaleCessOutPercent());

        var purchaseConfig = new CommodityResponse.PurchaseLedgerConfigDto(
                commodity.getPurchaseAcInStateId(),
                commodity.getPurchaseCgstPercent(),
                commodity.getPurchaseSgstPercent(),
                commodity.getPurchaseCessPercent(),
                commodity.getPurchaseAcOutStateId(),
                commodity.getPurchaseIgstPercent(),
                commodity.getPurchaseCessOutPercent());

        return new CommodityResponse(
                commodity.getItemId(),
                commodity.getItemName(),
                commodity.getHsnSacCode(),
                commodity.getDescription(),
                commodity.getUom(),
                commodity.getGstRateSale(),
                commodity.getGstRatePurchase(),
                commodity.getIsSalePurchaseActive(),
                salesConfig,
                purchaseConfig);
    }
}
