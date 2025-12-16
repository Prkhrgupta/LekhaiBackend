package in.lekhai.core.controller;

import in.lekhai.common.Result;
import in.lekhai.core.model.request.AdminRegistrationRequest;
import in.lekhai.core.model.request.CategoryCreationRequest;
import in.lekhai.core.model.request.ScreenFeatureCreationRequest;
import in.lekhai.core.model.request.SuperAdminRegistrationRequest;
import in.lekhai.core.model.request.*;
import in.lekhai.core.model.response.AdminRegistrationResponse;
import in.lekhai.core.model.response.CategoryCreationResponse;
import in.lekhai.core.model.response.CategoryResponse;
import in.lekhai.core.model.response.FeatureCreationResponse;
import in.lekhai.core.model.response.SuperAdminRegistrationResponse;
import in.lekhai.core.service.FeatureService;
import in.lekhai.core.service.SuperAdminService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final FeatureService featureService;

    public SuperAdminController(
            SuperAdminService superAdminService,
            FeatureService featureService
    ) {
        this.superAdminService = superAdminService;
        this.featureService = featureService;
    }

    @PostMapping("/register")
    public ResponseEntity<Result<?>> registerSuperAdmin(@RequestBody @Valid SuperAdminRegistrationRequest request) {
        SuperAdminRegistrationResponse response = superAdminService.registerSuperAdmin(request);
        Result<SuperAdminRegistrationResponse> registrationResponseResult = Result.success(response);
        return ResponseEntity.ok(registrationResponseResult);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<Result<?>> registerAdmin(@RequestBody @Valid AdminRegistrationRequest registrationRequest) {
        AdminRegistrationResponse adminRegistrationResponse = superAdminService.registerAdmin(registrationRequest);
        Result<AdminRegistrationResponse> success = Result.success(adminRegistrationResponse);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/category/create")
    public ResponseEntity<Result<?>> createCategory(@RequestBody @Valid CategoryCreationRequest request) {
        CategoryCreationResponse response = superAdminService.createCategory(request);
        return ResponseEntity.ok(Result.success(response));
    }

    @PostMapping("/feature/create")
    public ResponseEntity<Result<?>> createFeature(@RequestBody @Valid ScreenFeatureCreationRequest request) {
        FeatureCreationResponse featureCreationResponse = featureService.createScreenFeature(request);
        Result<FeatureCreationResponse> success = Result.success(featureCreationResponse);
        return ResponseEntity.ok(success);
    }

    @GetMapping("/features/all")
    public ResponseEntity<Result<?>> getAllFeatures() {
        List<FeatureResponse> allFeatures = featureService.getAllFeatures();
        return ResponseEntity.ok(Result.success(allFeatures));
    }

    @GetMapping("/category/list")
    public ResponseEntity<Result<List<CategoryResponse>>> getListOfCategories() {
        List<CategoryResponse> categoryResponses = superAdminService.listOfCategories();
        return ResponseEntity.ok(Result.success(categoryResponses));
    }

    @PostMapping("/category/enable/features")
    public ResponseEntity<Result<?>> enableFeaturesForCategory(@RequestBody @Valid EnableCategoryWiseFeatures request) {
        superAdminService.enableFeaturesForCategory(request);
        return ResponseEntity.ok(null);
    }
}
