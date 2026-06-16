package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.AreaApi;
import in.lekhai.contract.model.*;
import in.lekhai.core.account_master.service.AreaService;
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
public class AreaController implements AreaApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AreaService areaService;

    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    @Override
    public ResponseEntity<AreaResponse> createArea(@Valid AreaRequest request) {
        log.info("Got a request to create area {} :: {}", ShopContext.getShopCode(), request.toString());
        AreaResponse response = areaService.createArea(request);
        log.info("Successfully created area {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getAreaDropdownOptions() {
        log.info("Got a request to list all areas {}", ShopContext.getShopCode());
        List<DropdownItem> response = areaService.listAreas();
        log.info("Successfully listed all areas {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<AreaSummaryPageResponse> getAreaSummaries(@Valid AreaSearchableField areaSearchableField,
                                                                    @Valid String query,
                                                                    Pageable pageable) {
        log.info("Got a request to fetch area summary {}", ShopContext.getShopCode());
        AreaSummaryPageResponse response = areaService.listAreaSummaries(areaSearchableField, query, pageable);
        log.info("Successfully fetched area summary {} :: {}", ShopContext.getShopCode(), response.toString());
        return ResponseEntity.ok(response);
    }
}
