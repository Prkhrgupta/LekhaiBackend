package in.lekhai.core.controller.tenant;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.dto.tenant.CreateTenantExistingAdminRequest;
import in.lekhai.core.dto.tenant.CreateTenantNewAdminRequest;
import in.lekhai.core.dto.tenant.TenantCreationResponse;
import in.lekhai.core.service.tenant.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tenant")
@PreAuthorize(SecurityExpressions.IS_SUPER_ADMIN)
public class TenantController {
    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Result<?>> createTenantExisting(@RequestBody @Valid CreateTenantNewAdminRequest request) {
        TenantCreationResponse response = tenantService.createTenantWithNewAdmin(request);
        Result<TenantCreationResponse> success = Result.success(response);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/with-admin")
    public ResponseEntity<Result<?>> createTenantWithAdmin(@RequestBody @Valid CreateTenantExistingAdminRequest request) {
        TenantCreationResponse response = tenantService.createTenantWithExistingAdmin(request);
        Result<TenantCreationResponse> success = Result.success(response);
        return ResponseEntity.ok(success);
    }
}
