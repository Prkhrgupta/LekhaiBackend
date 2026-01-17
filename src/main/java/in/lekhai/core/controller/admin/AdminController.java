package in.lekhai.core.controller.admin;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.dto.admin.AdminRegistrationRequest;
import in.lekhai.core.dto.admin.AdminRegistrationResponse;
import in.lekhai.core.service.admin.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
