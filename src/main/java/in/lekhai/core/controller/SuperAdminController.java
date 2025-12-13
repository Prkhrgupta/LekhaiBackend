package in.lekhai.core.controller;

import in.lekhai.common.Result;
import in.lekhai.core.model.*;
import in.lekhai.core.service.FeatureService;
import in.lekhai.core.service.SuperAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final FeatureService featureService;

    public SuperAdminController(SuperAdminService superAdminService,
                                FeatureService featureService) {
        this.superAdminService = superAdminService;
        this.featureService = featureService;
    }

    @PostMapping("/register/admin")
    public ResponseEntity<Result<?>> registerAdmin(@RequestBody @Valid AdminRegistrationRequest registrationRequest) {
        AdminRegistrationResponse adminRegistrationResponse = superAdminService.registerAdmin(registrationRequest);
        Result<AdminRegistrationResponse> success = Result.success(adminRegistrationResponse);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/category/create")
    public ResponseEntity<Result<?>> createCategory(@RequestBody @Valid CategoryCreationRequest request) {
        superAdminService.createCategory(request);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/feature/create")
    public ResponseEntity<Result<?>> createFeature(@RequestBody @Valid ScreenFeatureCreationRequest request) {
        FeatureCreationResponse featureCreationResponse = featureService.createScreenFeature(request);
        Result<FeatureCreationResponse> success = Result.success(featureCreationResponse);
        return ResponseEntity.ok(success);
    }
}
