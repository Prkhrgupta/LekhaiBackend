package in.lekhai.core.inventory_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.inventory_master.dto.CommodityRequest;
import in.lekhai.core.inventory_master.dto.CommodityResponse;
import in.lekhai.core.inventory_master.service.CommodityService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commodity")
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class CommodityController {

    private final CommodityService commodityService;

    public CommodityController(CommodityService commodityService) {
        this.commodityService = commodityService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<CommodityResponse>> createCommodity(
            @RequestBody @Valid CommodityRequest request) {
        CommodityResponse response = commodityService.createCommodity(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Result<CommodityResponse>> updateCommodity(
            @PathVariable Long id,
            @RequestBody @Valid CommodityRequest request) {
        CommodityResponse response = commodityService.updateCommodity(id, request);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<CommodityResponse>> getCommodity(@PathVariable Long id) {
        CommodityResponse response = commodityService.getCommodity(id);
        return ResponseEntity.ok(Result.success(response));
    }

    @GetMapping("/list-all")
    public ResponseEntity<Result<List<CommodityResponse>>> listCommodities() {
        List<CommodityResponse> response = commodityService.listCommodities();
        return ResponseEntity.ok(Result.success(response));
    }
}
