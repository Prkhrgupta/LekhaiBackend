package in.lekhai.core.inventory_master.service;

import in.lekhai.core.inventory_master.domain.Commodity;
import in.lekhai.core.inventory_master.dto.CommodityRequest;
import in.lekhai.core.inventory_master.dto.CommodityResponse;
import in.lekhai.core.inventory_master.repository.CommodityRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopTransactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class CommodityService {

    private final CommodityRepository commodityRepository;

    public CommodityService(CommodityRepository commodityRepository) {
        this.commodityRepository = commodityRepository;
    }

    @ShopTransactional
    public CommodityResponse createCommodity(CommodityRequest request) {
        Commodity commodity = mapToEntity(request, new Commodity());
        Commodity saved = commodityRepository.save(commodity);
        return mapToResponse(saved);
    }

    @ShopTransactional
    public CommodityResponse updateCommodity(Long id, CommodityRequest request) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commodity not found with id: " + id));

        mapToEntity(request, commodity);
        Commodity saved = commodityRepository.save(commodity);
        return mapToResponse(saved);
    }

    @ShopTransactional
    public CommodityResponse getCommodity(Long id) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commodity not found with id: " + id));
        return mapToResponse(commodity);
    }

    @ShopTransactional
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
