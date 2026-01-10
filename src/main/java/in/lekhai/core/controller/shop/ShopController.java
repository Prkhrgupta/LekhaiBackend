package in.lekhai.core.controller.shop;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.dto.shop.CreateShopExistingAdminRequest;
import in.lekhai.core.dto.shop.CreateShopNewAdminRequest;
import in.lekhai.core.dto.shop.ShopCreationResponse;
import in.lekhai.core.service.shop.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shopCode")
@PreAuthorize(SecurityExpressions.IS_SUPER_ADMIN)
public class ShopController {
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping
    public ResponseEntity<Result<?>> createTenantExisting(@RequestBody @Valid CreateShopNewAdminRequest request) {
        ShopCreationResponse response = shopService.createTenantWithNewAdmin(request);
        Result<ShopCreationResponse> success = Result.success(response);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/with-admin")
    public ResponseEntity<Result<?>> createTenantWithAdmin(@RequestBody @Valid CreateShopExistingAdminRequest request) {
        ShopCreationResponse response = shopService.createTenantWithExistingAdmin(request);
        Result<ShopCreationResponse> success = Result.success(response);
        return ResponseEntity.ok(success);
    }
}
