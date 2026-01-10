package in.lekhai.core.controller.admin;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.dto.admin.AdminRegistrationRequest;
import in.lekhai.core.dto.admin.AdminRegistrationResponse;
import in.lekhai.core.service.admin.AdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize(SecurityExpressions.IS_ADMIN)
@Validated
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/register/new-admin")
    public ResponseEntity<Result<?>> registerAdmin(@RequestBody @Valid AdminRegistrationRequest registrationRequest) {
        AdminRegistrationResponse adminRegistrationResponse = adminService.registerAdmin(registrationRequest,
                null); // shopCode = null, this will be taken from JWT
        Result<AdminRegistrationResponse> success = Result.success(adminRegistrationResponse);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/switch-shopCode")
    public void switchCurrentTenant(@RequestParam("newTenant") @NotNull Integer newTenant) {
        // TODO : implementation to switch to available shopCode
        /*
            1. check if the shopCode switch is valid
            2. regenerate JWT token with new shopCode
         */
    }
}
