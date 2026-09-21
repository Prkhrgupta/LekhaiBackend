package in.lekhai.core.account_master.controller;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.contract.api.GeneralLedgerSettingApi;
import in.lekhai.contract.model.GeneralLedgerSettingRequest;
import in.lekhai.contract.model.GeneralLedgerSettingResponse;
import in.lekhai.core.account_master.service.GeneralLedgerSettingService;
import in.lekhai.shop.context.model.ShopContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize(SecurityExpressions.IS_SHOP_OWNER)
public class GeneralLedgerSettingController implements GeneralLedgerSettingApi {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final GeneralLedgerSettingService generalLedgerSettingService;

    public GeneralLedgerSettingController(GeneralLedgerSettingService generalLedgerSettingService) {
        this.generalLedgerSettingService = generalLedgerSettingService;
    }

    @Override
    public ResponseEntity<GeneralLedgerSettingResponse> getGeneralLedgerSetting() {
        log.info("Got a request to fetch general ledger setting {}", ShopContext.getShopCode());
        GeneralLedgerSettingResponse response = generalLedgerSettingService.getGeneralLedgerSetting();
        log.info("Successfully fetched general ledger setting {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GeneralLedgerSettingResponse> updateGeneralLedgerSetting(
            GeneralLedgerSettingRequest request) {
        log.info("Got a request to update general ledger setting {} :: {}", ShopContext.getShopCode(), request.toString());
        GeneralLedgerSettingResponse response = generalLedgerSettingService.updateGeneralLedgerSetting(request);
        log.info("Successfully updated general ledger setting {}", ShopContext.getShopCode());
        return ResponseEntity.ok(response);
    }
}
