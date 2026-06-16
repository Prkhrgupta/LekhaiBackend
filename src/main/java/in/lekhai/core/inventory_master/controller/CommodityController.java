package in.lekhai.core.inventory_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.CommodityApi;
import in.lekhai.contract.model.CommodityRequest;
import in.lekhai.contract.model.CommodityResponse;
import in.lekhai.contract.model.CommoditySummaryResponse;
import in.lekhai.core.inventory_master.service.CommodityService;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class CommodityController implements CommodityApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final CommodityService commodityService;

    public CommodityController(CommodityService commodityService) {
        this.commodityService = commodityService;
    }

    @Override
    public ResponseEntity<CommodityResponse> createCommodity(CommodityRequest request) {
        log.info("Got a request to create commodity {} :: {}", ShopContext.getShopCode(), request.toString());
        CommodityResponse response = commodityService.createCommodity(request);
        log.info("Successfully created commodity {} :: item id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> deleteCommodity(Long id) {
        log.info("Got a request to delete commodity {} :: commodity id {}", ShopContext.getShopCode(), id);
        commodityService.deleteCommodity(id);
        log.info("Successfully deleted commodity {} :: commodity id {}", ShopContext.getShopCode(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CommodityResponse> getCommodity(Long id) {
        log.info("Got a request to fetch commodity {} :: commodity id {}", ShopContext.getShopCode(), id);
        CommodityResponse response = commodityService.getCommodityById(id);
        log.info("Successfully fetched commodity {} :: item id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<CommodityResponse>> listCommodities() {
        log.info("Got a request to list all commodities {}", ShopContext.getShopCode());
        List<CommodityResponse> response = commodityService.listCommodities();
        log.info("Successfully listed all commodities {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommodityResponse> updateCommodity(Long id, CommodityRequest request) {
        log.info("Got a request to update commodity {} :: {}", ShopContext.getShopCode(), request.toString());
        CommodityResponse response = commodityService.updateCommodity(id, request);
        log.info("Successfully updated commodity {} :: item id {}", ShopContext.getShopCode(), response.getId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CommoditySummaryResponse> getCommoditySummaries() {
        log.info("Got a request to fetch commodity summary {}", ShopContext.getShopCode());
        CommoditySummaryResponse response = commodityService.listCommoditySummaries();
        log.info("Successfully fetched commodity summary {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
