package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.TdsSectionApi;
import in.lekhai.contract.model.DropdownItem;
import in.lekhai.core.account_master.service.TdsSectionService;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class TdsSectionController implements TdsSectionApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final TdsSectionService tdsSectionService;

    public TdsSectionController(TdsSectionService tdsSectionService) {
        this.tdsSectionService = tdsSectionService;
    }

    @Override
    public ResponseEntity<List<DropdownItem>> getTdsSectionDropdownOptions() {
        log.info("Got a request to list all TDS sections {}", ShopContext.getShopCode());
        List<DropdownItem> response = tdsSectionService.listTdsSections();
        log.info("Successfully listed all TDS sections {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
