package in.lekhai.core.controller.superadmin;

import in.lekhai.authentication.utils.SecurityExpressions;
import in.lekhai.common.Result;
import in.lekhai.core.dto.superadmin.SuperAdminRegistrationRequest;
import in.lekhai.core.dto.superadmin.SuperAdminRegistrationResponse;
import in.lekhai.core.service.superadmin.SuperAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/superadmin")
@PreAuthorize(SecurityExpressions.IS_SUPER_ADMIN)
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    public SuperAdminController(
            SuperAdminService superAdminService
    ) {
        this.superAdminService = superAdminService;
    }

    @PostMapping("/register")
    public ResponseEntity<Result<?>> registerSuperAdmin(@RequestBody @Valid SuperAdminRegistrationRequest request) {
        SuperAdminRegistrationResponse response = superAdminService.registerSuperAdmin(request);
        Result<SuperAdminRegistrationResponse> registrationResponseResult = Result.success(response);
        return ResponseEntity.ok(registrationResponseResult);
    }

}