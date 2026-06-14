package in.lekhai.core.inventory_master.service;

import in.lekhai.contract.model.CommodityRequest;
import in.lekhai.contract.model.CommodityResponse;
import in.lekhai.contract.model.CommoditySummaryColumn;
import in.lekhai.contract.model.CommoditySummaryItem;
import in.lekhai.contract.model.CommoditySummaryResponse;
import in.lekhai.contract.model.PurchaseInStateRequest;
import in.lekhai.contract.model.PurchaseInStateResponse;
import in.lekhai.contract.model.PurchaseLedgerRequest;
import in.lekhai.contract.model.PurchaseLedgerResponse;
import in.lekhai.contract.model.PurchaseOutStateRequest;
import in.lekhai.contract.model.PurchaseOutStateResponse;
import in.lekhai.contract.model.SaleInStateRequest;
import in.lekhai.contract.model.SaleInStateResponse;
import in.lekhai.contract.model.SaleLedgerRequest;
import in.lekhai.contract.model.SaleLedgerResponse;
import in.lekhai.contract.model.SaleOutStateRequest;
import in.lekhai.contract.model.SaleOutStateResponse;
import in.lekhai.core.inventory_master.domain.Commodity;
import in.lekhai.core.inventory_master.repository.CommodityRepository;
import in.lekhai.shop.context.transaction.manager.annotation.ShopContextTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class CommodityService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CommodityRepository commodityRepository;

    public CommodityService(CommodityRepository commodityRepository) {
        this.commodityRepository = commodityRepository;
    }

    @ShopContextTransactional
    public CommodityResponse createCommodity(CommodityRequest request) {
        Commodity commodity = mapToEntity(request, new Commodity());
        Commodity saved = commodityRepository.save(commodity);
        log.info("Saved commodity :: item id {}", saved.getItemId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public CommodityResponse updateCommodity(Long id, CommodityRequest request) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commodity not found with id: " + id));
        mapToEntity(request, commodity);
        Commodity saved = commodityRepository.save(commodity);
        log.info("Updated commodity :: item id {}", saved.getItemId());
        return mapToResponse(saved);
    }

    @ShopContextTransactional
    public CommodityResponse getCommodityById(Long id) {
        Commodity commodity = commodityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commodity not found with id: " + id));
        return mapToResponse(commodity);
    }

    @ShopContextTransactional
    public List<CommodityResponse> listCommodities() {
        return StreamSupport.stream(commodityRepository.findAll().spliterator(), false)
                .filter(commodity -> !Boolean.TRUE.equals(commodity.getDeleted()))
                .map(this::mapToResponse)
                .toList();
    }

    @ShopContextTransactional
    public CommoditySummaryResponse listCommoditySummaries() {
        List<CommoditySummaryColumn> columns = List.of(
                new CommoditySummaryColumn().name("ID").type("number").width(80),
                new CommoditySummaryColumn().name("Name").type("text").width(200),
                new CommoditySummaryColumn().name("HSN/SAC").type("text").width(120),
                new CommoditySummaryColumn().name("UOM").type("text").width(100),
                new CommoditySummaryColumn().name("GST Rate Sale").type("number").width(120),
                new CommoditySummaryColumn().name("GST Rate Purchase").type("number").width(140)
        );

        List<CommoditySummaryItem> data = StreamSupport
                .stream(commodityRepository.findAll().spliterator(), false)
                .filter(commodity -> !Boolean.TRUE.equals(commodity.getDeleted()))
                .map(commodity -> new CommoditySummaryItem()
                        .id(commodity.getItemId())
                        .name(commodity.getItemName())
                        .hsnSacCode(commodity.getHsnSacCode())
                        .unitOfMeasurement(commodity.getUom())
                        .gstRateSale(toDouble(commodity.getGstRateSale()))
                        .gstRatePurchase(toDouble(commodity.getGstRatePurchase())))
                .toList();

        return new CommoditySummaryResponse().columns(columns).data(data);
    }

    private Commodity mapToEntity(CommodityRequest request, Commodity commodity) {
        commodity.setItemName(request.getName());
        commodity.setHsnSacCode(request.getHsnSacCode());
        commodity.setDescription(request.getDescription());
        commodity.setUom(request.getUnitOfMeasurement());
        commodity.setGstRateSale(toBigDecimal(request.getGstRateSale()));
        commodity.setGstRatePurchase(toBigDecimal(request.getGstRatePurchase()));
        commodity.setIsSalePurchaseActive(request.getSalePurchaseSetting());

        applySaleLedger(request.getSaleLedger(), commodity);
        applyPurchaseLedger(request.getPurchaseLedger(), commodity);

        return commodity;
    }

    private void applySaleLedger(SaleLedgerRequest saleLedger, Commodity commodity) {
        if (saleLedger == null) {
            return;
        }
        SaleInStateRequest inState = saleLedger.getInState();
        if (inState != null) {
            commodity.setSaleAcInStateId(inState.getSaleAccount());
            commodity.setSaleCgstPercent(toBigDecimal(inState.getCgstPercentage()));
            commodity.setSaleSgstPercent(toBigDecimal(inState.getSgstPercentage()));
            commodity.setSaleCessPercent(toBigDecimal(inState.getCessPercentage()));
        }
        SaleOutStateRequest outState = saleLedger.getOutState();
        if (outState != null) {
            commodity.setSaleAcOutStateId(outState.getSaleAccount());
            commodity.setSaleIgstPercent(toBigDecimal(outState.getIgstPercentage()));
            commodity.setSaleCessOutPercent(toBigDecimal(outState.getCessPercentage()));
        }
        commodity.setRoundOffAcId(saleLedger.getRoundOff());
    }

    private void applyPurchaseLedger(PurchaseLedgerRequest purchaseLedger, Commodity commodity) {
        if (purchaseLedger == null) {
            return;
        }
        PurchaseInStateRequest inState = purchaseLedger.getInState();
        if (inState != null) {
            commodity.setPurchaseAcInStateId(inState.getPurchaseAccount());
            commodity.setPurchaseCgstPercent(toBigDecimal(inState.getCgstPercentage()));
            commodity.setPurchaseSgstPercent(toBigDecimal(inState.getSgstPercentage()));
            commodity.setPurchaseCessPercent(toBigDecimal(inState.getCessPercentage()));
        }
        PurchaseOutStateRequest outState = purchaseLedger.getOutState();
        if (outState != null) {
            commodity.setPurchaseAcOutStateId(outState.getPurchaseAccount());
            commodity.setPurchaseIgstPercent(toBigDecimal(outState.getIgstPercentage()));
            commodity.setPurchaseCessOutPercent(toBigDecimal(outState.getCessPercentage()));
        }
    }

    private CommodityResponse mapToResponse(Commodity commodity) {
        SaleLedgerResponse saleLedger = new SaleLedgerResponse()
                .inState(new SaleInStateResponse()
                        .saleAccount(commodity.getSaleAcInStateId())
                        .cgstPercentage(toDouble(commodity.getSaleCgstPercent()))
                        .cgstAccount(null)
                        .sgstPercentage(toDouble(commodity.getSaleSgstPercent()))
                        .sgstAccount(null)
                        .cessPercentage(toDouble(commodity.getSaleCessPercent()))
                        .cessAccount(null))
                .outState(new SaleOutStateResponse()
                        .saleAccount(commodity.getSaleAcOutStateId())
                        .igstPercentage(toDouble(commodity.getSaleIgstPercent()))
                        .igstAccount(null)
                        .cessPercentage(toDouble(commodity.getSaleCessOutPercent()))
                        .cessAccount(null))
                .roundOff(commodity.getRoundOffAcId());

        PurchaseLedgerResponse purchaseLedger = new PurchaseLedgerResponse()
                .inState(new PurchaseInStateResponse()
                        .purchaseAccount(commodity.getPurchaseAcInStateId())
                        .cgstPercentage(toDouble(commodity.getPurchaseCgstPercent()))
                        .cgstAccount(null)
                        .sgstPercentage(toDouble(commodity.getPurchaseSgstPercent()))
                        .sgstAccount(null)
                        .cessPercentage(toDouble(commodity.getPurchaseCessPercent()))
                        .cessAccount(null))
                .outState(new PurchaseOutStateResponse()
                        .purchaseAccount(commodity.getPurchaseAcOutStateId())
                        .igstPercentage(toDouble(commodity.getPurchaseIgstPercent()))
                        .igstAccount(null)
                        .cessPercentage(toDouble(commodity.getPurchaseCessOutPercent()))
                        .cessAccount(null));

        return new CommodityResponse()
                .id(commodity.getItemId())
                .name(commodity.getItemName())
                .hsnSacCode(commodity.getHsnSacCode())
                .description(commodity.getDescription())
                .gstRateSale(toDouble(commodity.getGstRateSale()))
                .gstRatePurchase(toDouble(commodity.getGstRatePurchase()))
                .unitOfMeasurement(commodity.getUom())
                .salePurchaseSetting(commodity.getIsSalePurchaseActive())
                .saleLedger(saleLedger)
                .purchaseLedger(purchaseLedger)
                .isActive(!Boolean.TRUE.equals(commodity.getDeleted()))
                .createdAt(toOffsetDateTime(commodity.getCreatedAt()));
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    private static java.time.OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
